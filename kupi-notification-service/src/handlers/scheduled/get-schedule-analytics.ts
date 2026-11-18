import { APIGatewayProxyEvent, APIGatewayProxyResult } from 'aws-lambda';
import { getClient } from '../../services/database/connection';
import { successResponse, internalServerError } from '../../middleware/response-wrapper';
import { Logger } from '../../utils/logger';

export const handler = async (
  event: APIGatewayProxyEvent
): Promise<APIGatewayProxyResult> => {
  const logger = new Logger('ScheduleAnalyticsHandler');
  let client;

  try {
    logger.info('Getting scheduled template analytics...');

    client = await getClient();

    // Get overall analytics
    const analyticsQuery = `
      SELECT * FROM scheduled_template_analytics
      ORDER BY schedule_day_of_month, schedule_time
    `;

    const analyticsResult = await client.query(analyticsQuery);

    // Get recent executions (last 7 days)
    const recentExecutionsQuery = `
      SELECT
        ste.*,
        nt.title,
        nt.schedule_target_audience
      FROM scheduled_template_executions ste
      INNER JOIN notification_templates nt ON ste.template_id = nt.id
      WHERE ste.executed_at >= CURRENT_DATE - INTERVAL '7 days'
      ORDER BY ste.executed_at DESC
      LIMIT 50
    `;

    const recentExecutionsResult = await client.query(recentExecutionsQuery);

    // Get execution statistics
    const statsQuery = `
      SELECT
        COUNT(*) as total_executions,
        COUNT(CASE WHEN status = 'completed' THEN 1 END) as successful_executions,
        COUNT(CASE WHEN status = 'failed' THEN 1 END) as failed_executions,
        SUM(target_user_count) as total_users_targeted,
        SUM(successful_sends) as total_successful_sends,
        SUM(failed_sends) as total_failed_sends,
        AVG(execution_duration_ms) as avg_execution_duration_ms,
        MAX(executed_at) as last_execution
      FROM scheduled_template_executions
      WHERE executed_at >= CURRENT_DATE - INTERVAL '30 days'
    `;

    const statsResult = await client.query(statsQuery);

    // Get next scheduled executions (upcoming in next 7 days)
    const upcomingQuery = `
      SELECT
        template_code,
        title,
        schedule_day_of_month,
        schedule_time,
        schedule_target_audience,
        CASE
          WHEN schedule_day_of_month >= EXTRACT(DAY FROM CURRENT_DATE) THEN
            DATE(DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '1 day' * (schedule_day_of_month - 1)) + schedule_time
          ELSE
            DATE(DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '1 month' + INTERVAL '1 day' * (schedule_day_of_month - 1)) + schedule_time
        END as next_execution
      FROM notification_templates
      WHERE schedule_enabled = true AND is_active = true
      ORDER BY next_execution
      LIMIT 20
    `;

    const upcomingResult = await client.query(upcomingQuery);

    const response = {
      analytics: analyticsResult.rows.map(row => ({
        template_code: row.template_code,
        title: row.title,
        schedule: {
          day_of_month: row.schedule_day_of_month,
          time: row.schedule_time,
          target_audience: row.schedule_target_audience
        },
        execution_stats: {
          total_executions: parseInt(row.total_executions || '0'),
          successful_executions: parseInt(row.successful_executions || '0'),
          failed_executions: parseInt(row.failed_executions || '0'),
          avg_target_users: Math.round(parseFloat(row.avg_target_users || '0')),
          total_users_targeted: parseInt(row.total_users_targeted || '0'),
          total_successful_sends: parseInt(row.total_successful_sends || '0'),
          total_failed_sends: parseInt(row.total_failed_sends || '0'),
          avg_execution_duration_ms: Math.round(parseFloat(row.avg_execution_duration_ms || '0')),
          last_executed_at: row.last_executed_at
        }
      })),
      recent_executions: recentExecutionsResult.rows.map(row => ({
        id: row.id,
        template_code: row.template_code,
        title: row.title,
        executed_at: row.executed_at,
        target_user_count: row.target_user_count,
        successful_sends: row.successful_sends,
        failed_sends: row.failed_sends,
        status: row.status,
        execution_duration_ms: row.execution_duration_ms,
        error_message: row.error_message,
        target_audience: row.schedule_target_audience
      })),
      statistics: statsResult.rows[0] ? {
        total_executions: parseInt(statsResult.rows[0].total_executions || '0'),
        successful_executions: parseInt(statsResult.rows[0].successful_executions || '0'),
        failed_executions: parseInt(statsResult.rows[0].failed_executions || '0'),
        total_users_targeted: parseInt(statsResult.rows[0].total_users_targeted || '0'),
        total_successful_sends: parseInt(statsResult.rows[0].total_successful_sends || '0'),
        total_failed_sends: parseInt(statsResult.rows[0].total_failed_sends || '0'),
        avg_execution_duration_ms: Math.round(parseFloat(statsResult.rows[0].avg_execution_duration_ms || '0')),
        last_execution: statsResult.rows[0].last_execution,
        success_rate: statsResult.rows[0].successful_executions && statsResult.rows[0].total_executions
          ? Math.round((parseInt(statsResult.rows[0].successful_executions) / parseInt(statsResult.rows[0].total_executions)) * 100)
          : 0
      } : null,
      upcoming_executions: upcomingResult.rows.map(row => ({
        template_code: row.template_code,
        title: row.title,
        schedule: {
          day_of_month: row.schedule_day_of_month,
          time: row.schedule_time,
          target_audience: row.schedule_target_audience
        },
        next_execution: row.next_execution
      }))
    };

    logger.info(`Retrieved analytics for ${response.analytics.length} scheduled templates`);

    return successResponse(
      response,
      'Scheduled template analytics retrieved successfully'
    );

  } catch (error: any) {
    logger.error('Failed to get scheduled template analytics:', error);
    return internalServerError(
      'Failed to retrieve analytics',
      'Please try again later'
    );
  } finally {
    if (client) {
      client.release();
    }
  }
};