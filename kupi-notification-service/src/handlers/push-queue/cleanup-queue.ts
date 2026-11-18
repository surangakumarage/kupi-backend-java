import { ScheduledHandler } from 'aws-lambda';
import { getClient } from '../../services/database/connection';
import { Logger } from '../../utils/logger';

interface CleanupResult {
  queueRecordsDeleted: number;
  logsRecordsDeleted: number;
  deviceTokensDeactivated: number;
  errors: string[];
}

export const handler: ScheduledHandler = async (event, context) => {
  const logger = new Logger('PushQueueCleanupProcessor');
  let client;

  try {
    logger.info('Starting push notification queue cleanup...', {
      time: new Date().toISOString(),
      event: event
    });

    client = await getClient();

    const result: CleanupResult = {
      queueRecordsDeleted: 0,
      logsRecordsDeleted: 0,
      deviceTokensDeactivated: 0,
      errors: []
    };

    // 1. Cleanup old queue records using the database function
    try {
      const cleanupQueueQuery = `SELECT cleanup_old_queue_records() as deleted_count`;
      const cleanupResult = await client.query(cleanupQueueQuery);
      result.queueRecordsDeleted = cleanupResult.rows[0].deleted_count;

      logger.info(`Cleaned up ${result.queueRecordsDeleted} old queue records`);
    } catch (error: any) {
      const errorMsg = `Failed to cleanup queue records: ${error.message}`;
      logger.error(errorMsg, error);
      result.errors.push(errorMsg);
    }

    // 2. Cleanup old push notification logs (keep last 60 days)
    try {
      const cleanupLogsQuery = `
        DELETE FROM push_notification_logs
        WHERE sent_at < CURRENT_TIMESTAMP - INTERVAL '60 days'
      `;
      const logsResult = await client.query(cleanupLogsQuery);
      result.logsRecordsDeleted = logsResult.rowCount || 0;

      logger.info(`Cleaned up ${result.logsRecordsDeleted} old push notification logs`);
    } catch (error: any) {
      const errorMsg = `Failed to cleanup push logs: ${error.message}`;
      logger.error(errorMsg, error);
      result.errors.push(errorMsg);
    }

    // 3. Deactivate unused device tokens (not used in last 90 days)
    try {
      const deactivateTokensQuery = `
        UPDATE user_device_tokens
        SET is_active = false,
            updated_at = CURRENT_TIMESTAMP
        WHERE is_active = true
          AND (last_used_at IS NULL OR last_used_at < CURRENT_TIMESTAMP - INTERVAL '90 days')
          AND created_at < CURRENT_TIMESTAMP - INTERVAL '90 days'
      `;
      const tokensResult = await client.query(deactivateTokensQuery);
      result.deviceTokensDeactivated = tokensResult.rowCount || 0;

      logger.info(`Deactivated ${result.deviceTokensDeactivated} unused device tokens`);
    } catch (error: any) {
      const errorMsg = `Failed to deactivate device tokens: ${error.message}`;
      logger.error(errorMsg, error);
      result.errors.push(errorMsg);
    }

    // 4. Cleanup failed messages older than 7 days (they won't be retried anymore)
    try {
      const cleanupFailedQuery = `
        DELETE FROM push_message_queue
        WHERE status = 'FAILED'
          AND failed_at < CURRENT_TIMESTAMP - INTERVAL '7 days'
      `;
      const failedResult = await client.query(cleanupFailedQuery);
      const deletedFailed = failedResult.rowCount || 0;
      result.queueRecordsDeleted += deletedFailed;

      logger.info(`Cleaned up ${deletedFailed} old failed queue records`);
    } catch (error: any) {
      const errorMsg = `Failed to cleanup failed messages: ${error.message}`;
      logger.error(errorMsg, error);
      result.errors.push(errorMsg);
    }

    // 5. Update queue analytics if you have any analytics tables
    try {
      await updateQueueAnalytics(client, logger);
    } catch (error: any) {
      const errorMsg = `Failed to update queue analytics: ${error.message}`;
      logger.error(errorMsg, error);
      result.errors.push(errorMsg);
    }

    logger.info('Push notification queue cleanup completed', {
      queueRecordsDeleted: result.queueRecordsDeleted,
      logsRecordsDeleted: result.logsRecordsDeleted,
      deviceTokensDeactivated: result.deviceTokensDeactivated,
      errorCount: result.errors.length
    });

    return {
      statusCode: 200,
      body: JSON.stringify({
        message: 'Push notification queue cleanup completed',
        ...result,
        success: result.errors.length === 0
      })
    };

  } catch (error: any) {
    logger.error('Failed to run push notification queue cleanup:', error);
    throw error;
  } finally {
    if (client) {
      client.release();
    }
  }
};

async function updateQueueAnalytics(client: any, logger: Logger): Promise<void> {
  try {
    // Create or update a summary table for queue analytics
    const analyticsQuery = `
      INSERT INTO push_queue_analytics (
        analysis_date,
        total_messages_sent,
        total_messages_failed,
        unique_users_reached,
        top_template_codes,
        platform_breakdown,
        created_at
      )
      SELECT
        CURRENT_DATE as analysis_date,
        COUNT(CASE WHEN status = 'SENT' THEN 1 END) as total_messages_sent,
        COUNT(CASE WHEN status = 'FAILED' THEN 1 END) as total_messages_failed,
        COUNT(DISTINCT user_id) as unique_users_reached,
        (
          SELECT json_agg(json_build_object('template_code', template_code, 'count', count))
          FROM (
            SELECT template_code, COUNT(*) as count
            FROM push_message_queue
            WHERE created_at >= CURRENT_DATE - INTERVAL '1 day'
              AND status IN ('SENT', 'FAILED')
            GROUP BY template_code
            ORDER BY count DESC
            LIMIT 10
          ) top_templates
        ) as top_template_codes,
        (
          SELECT json_agg(json_build_object('platform', platform, 'count', count))
          FROM (
            SELECT pnl.platform, COUNT(*) as count
            FROM push_notification_logs pnl
            WHERE pnl.sent_at >= CURRENT_DATE - INTERVAL '1 day'
            GROUP BY pnl.platform
          ) platform_stats
        ) as platform_breakdown,
        CURRENT_TIMESTAMP as created_at
      FROM push_message_queue
      WHERE created_at >= CURRENT_DATE - INTERVAL '1 day'
      ON CONFLICT (analysis_date) DO UPDATE SET
        total_messages_sent = EXCLUDED.total_messages_sent,
        total_messages_failed = EXCLUDED.total_messages_failed,
        unique_users_reached = EXCLUDED.unique_users_reached,
        top_template_codes = EXCLUDED.top_template_codes,
        platform_breakdown = EXCLUDED.platform_breakdown,
        created_at = EXCLUDED.created_at
    `;

    // Only create analytics if the table exists
    const tableExistsQuery = `
      SELECT EXISTS (
        SELECT FROM information_schema.tables
        WHERE table_name = 'push_queue_analytics'
      ) as table_exists
    `;

    const tableResult = await client.query(tableExistsQuery);

    if (tableResult.rows[0].table_exists) {
      await client.query(analyticsQuery);
      logger.info('Updated push queue analytics');
    } else {
      logger.info('Push queue analytics table does not exist, skipping analytics update');
    }

  } catch (error: any) {
    // Don't fail the whole cleanup if analytics update fails
    logger.warn(`Failed to update queue analytics: ${error.message}`);
  }
}

// Optional: Create analytics table if it doesn't exist
export async function createAnalyticsTable(client: any): Promise<void> {
  const createTableQuery = `
    CREATE TABLE IF NOT EXISTS push_queue_analytics (
      id SERIAL PRIMARY KEY,
      analysis_date DATE UNIQUE NOT NULL,
      total_messages_sent INTEGER DEFAULT 0,
      total_messages_failed INTEGER DEFAULT 0,
      unique_users_reached INTEGER DEFAULT 0,
      top_template_codes JSONB,
      platform_breakdown JSONB,
      created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
    );

    CREATE INDEX IF NOT EXISTS idx_push_analytics_date
      ON push_queue_analytics(analysis_date);
  `;

  await client.query(createTableQuery);
}