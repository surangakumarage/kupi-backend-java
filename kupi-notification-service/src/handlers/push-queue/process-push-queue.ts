import { ScheduledHandler } from 'aws-lambda';
import { getClient } from '../../services/database/connection';
import { Logger } from '../../utils/logger';
import * as admin from 'firebase-admin';

// Initialize Firebase Admin SDK (do this once)
if (!admin.apps.length) {
  admin.initializeApp({
    credential: admin.credential.cert({
      projectId: process.env.FCM_PROJECT_ID,
      privateKey: process.env.FCM_PRIVATE_KEY?.replace(/\\n/g, '\n'),
      clientEmail: process.env.FCM_CLIENT_EMAIL,
    })
  });
}

interface QueueMessage {
  id: string;
  job_id: string;
  user_id: number;
  template_code: string;
  payload: any;
  retry_count: number;
  max_retries: number;
  scheduled_at: Date;
  created_at: Date;
}

interface UserDeviceToken {
  id: number;
  user_id: number;
  device_token: string;
  platform: string;
  is_active: boolean;
}

export const handler: ScheduledHandler = async (event, context) => {
  const logger = new Logger('PushQueueProcessor');
  let client;

  try {
    logger.info('Starting push notification queue processing...', {
      time: new Date().toISOString(),
      event: event
    });

    client = await getClient();

    // 1. Get pending messages that are due to be sent
    const pendingMessagesQuery = `
      SELECT
        id,
        job_id,
        user_id,
        template_code,
        payload,
        retry_count,
        max_retries,
        scheduled_at,
        created_at
      FROM push_message_queue
      WHERE status = 'PENDING'
        AND scheduled_at <= CURRENT_TIMESTAMP
      ORDER BY scheduled_at ASC, created_at ASC
      LIMIT 100  -- Process in batches
    `;

    const messagesResult = await client.query(pendingMessagesQuery);
    const pendingMessages: QueueMessage[] = messagesResult.rows;

    logger.info(`Found ${pendingMessages.length} pending messages to process`);

    if (pendingMessages.length === 0) {
      logger.info('No pending messages found, ending processing');
      return {
        statusCode: 200,
        body: JSON.stringify({
          message: 'No pending messages to process',
          processedMessages: 0
        })
      };
    }

    let successCount = 0;
    let failedCount = 0;
    let skippedCount = 0;

    // 2. Process each message
    for (const message of pendingMessages) {
      try {
        // Mark as processing to prevent duplicate processing
        await updateMessageStatus(message.id, 'PROCESSING', client);

        const result = await processMessage(message, client, logger);

        if (result.success) {
          await updateMessageStatus(message.id, 'SENT', client, {
            sent_at: new Date(),
            response_data: result.response
          });
          successCount++;
        } else {
          await handleFailedMessage(message, result.error, client, logger);
          failedCount++;
        }

      } catch (error) {
        logger.error(`Failed to process message ${message.job_id}:`, error);
        await handleFailedMessage(message, error, client, logger);
        failedCount++;
      }
    }

    logger.info(`Push queue processing completed`, {
      total: pendingMessages.length,
      success: successCount,
      failed: failedCount,
      skipped: skippedCount
    });

    return {
      statusCode: 200,
      body: JSON.stringify({
        message: 'Push queue processing completed',
        total: pendingMessages.length,
        success: successCount,
        failed: failedCount,
        skipped: skippedCount
      })
    };

  } catch (error: any) {
    logger.error('Failed to process push notification queue:', error);
    throw error;
  } finally {
    if (client) {
      client.release();
    }
  }
};

async function processMessage(
  message: QueueMessage,
  client: any,
  logger: Logger
): Promise<{ success: boolean; response?: any; error?: any }> {

  logger.info(`Processing message ${message.job_id}`, {
    userId: message.user_id,
    templateCode: message.template_code,
    retryCount: message.retry_count
  });

  try {
    // 1. Get user's device tokens
    const deviceTokensQuery = `
      SELECT
        id,
        user_id,
        token as device_token,
        device_type as platform,
        is_active
      FROM fcm_tokens
      WHERE user_id = $1
        AND is_active = true
    `;

    const tokensResult = await client.query(deviceTokensQuery, [message.user_id]);
    const deviceTokens: UserDeviceToken[] = tokensResult.rows;

    if (deviceTokens.length === 0) {
      logger.warn(`No active device tokens found for user ${message.user_id}`);
      return {
        success: false,
        error: 'No active device tokens found'
      };
    }

    // 2. Send push notifications to all devices
    const sendResults = await Promise.allSettled(
      deviceTokens.map(deviceToken =>
        sendPushNotification(deviceToken, message.payload, logger)
      )
    );

    // 3. Log individual push notification attempts
    for (let i = 0; i < deviceTokens.length; i++) {
      const deviceToken = deviceTokens[i];
      const result = sendResults[i];

      await logPushNotification(
        message,
        deviceToken,
        result.status === 'fulfilled' ? result.value : result.reason,
        client,
        logger
      );
    }

    // 4. Check if at least one notification was sent successfully
    const successfulSends = sendResults.filter(result => result.status === 'fulfilled').length;

    if (successfulSends === 0) {
      return {
        success: false,
        error: 'All push notification attempts failed',
        response: { attempts: deviceTokens.length, failures: sendResults.length }
      };
    }

    return {
      success: true,
      response: {
        deviceTokens: deviceTokens.length,
        successfulSends,
        failedSends: sendResults.length - successfulSends
      }
    };

  } catch (error) {
    logger.error(`Error processing message ${message.job_id}:`, error);
    return { success: false, error };
  }
}

async function sendPushNotification(
  deviceToken: UserDeviceToken,
  payload: any,
  logger: Logger
): Promise<any> {

  try {
    // Prepare the Firebase message
    const firebaseMessage = {
      token: deviceToken.device_token,
      notification: payload.notification,
      data: payload.data || {},
      android: payload.android,
      apns: payload.apns
    };

    logger.debug(`Sending push notification`, {
      userId: deviceToken.user_id,
      platform: deviceToken.platform,
      token: deviceToken.device_token.substring(0, 20) + '...' // Log partial token for debugging
    });

    // Send via Firebase Admin SDK
    const response = await admin.messaging().send(firebaseMessage);

    logger.info(`Push notification sent successfully`, {
      userId: deviceToken.user_id,
      platform: deviceToken.platform,
      messageId: response
    });

    return {
      success: true,
      messageId: response,
      platform: deviceToken.platform
    };

  } catch (error: any) {
    logger.error(`Failed to send push notification`, {
      userId: deviceToken.user_id,
      platform: deviceToken.platform,
      error: error.message
    });

    // Handle specific Firebase errors
    if (error.code === 'messaging/registration-token-not-registered' ||
        error.code === 'messaging/invalid-registration-token') {
      // Mark device token as inactive
      await markDeviceTokenInactive(deviceToken.id);
      logger.warn(`Marked device token as inactive due to Firebase error: ${error.code}`);
    }

    throw error;
  }
}

async function logPushNotification(
  message: QueueMessage,
  deviceToken: UserDeviceToken,
  result: any,
  client: any,
  logger: Logger
): Promise<void> {

  try {
    const logQuery = `
      INSERT INTO push_notification_logs (
        queue_id,
        user_id,
        template_code,
        push_token,
        platform,
        message_title,
        message_body,
        payload,
        response_status,
        response_body
      ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)
    `;

    const responseStatus = result.success ? 'sent' : 'failed';
    const responseBody = result.success ? result : { error: result.message || 'Unknown error' };

    await client.query(logQuery, [
      message.id,
      message.user_id,
      message.template_code,
      deviceToken.device_token,
      deviceToken.platform,
      message.payload.notification?.title,
      message.payload.notification?.body,
      message.payload,
      responseStatus,
      JSON.stringify(responseBody)
    ]);

  } catch (error) {
    // Don't fail the whole process if logging fails
    logger.warn(`Failed to log push notification result: ${error}`);
  }
}

async function updateMessageStatus(
  messageId: string,
  status: string,
  client: any,
  additionalData?: { sent_at?: Date; failed_at?: Date; response_data?: any; error_message?: string }
): Promise<void> {

  const updateFields = ['status = $2'];
  const updateValues = [messageId, status];
  let paramCount = 2;

  if (status === 'PROCESSING') {
    updateFields.push('started_processing_at = CURRENT_TIMESTAMP');
  }

  if (additionalData?.sent_at) {
    paramCount++;
    updateFields.push(`sent_at = $${paramCount}`);
    updateValues.push(additionalData.sent_at);
  }

  if (additionalData?.failed_at) {
    paramCount++;
    updateFields.push(`failed_at = $${paramCount}`);
    updateValues.push(additionalData.failed_at);
  }

  if (additionalData?.error_message) {
    paramCount++;
    updateFields.push(`error_message = $${paramCount}`);
    updateValues.push(additionalData.error_message);
  }

  const updateQuery = `
    UPDATE push_message_queue
    SET ${updateFields.join(', ')}
    WHERE id = $1
  `;

  await client.query(updateQuery, updateValues);
}

async function handleFailedMessage(
  message: QueueMessage,
  error: any,
  client: any,
  logger: Logger
): Promise<void> {

  const retryCount = message.retry_count + 1;
  const errorMessage = error?.message || error?.toString() || 'Unknown error';

  if (retryCount <= message.max_retries) {
    // Calculate exponential backoff (2^retry * 5 minutes)
    const delayMinutes = Math.pow(2, retryCount) * 5;
    const nextRetryTime = new Date(Date.now() + (delayMinutes * 60 * 1000));

    logger.info(`Scheduling retry ${retryCount}/${message.max_retries} for message ${message.job_id}`, {
      delayMinutes,
      nextRetryTime
    });

    await client.query(`
      UPDATE push_message_queue
      SET status = 'PENDING',
          retry_count = $2,
          error_message = $3,
          scheduled_at = $4
      WHERE id = $1
    `, [message.id, retryCount, errorMessage, nextRetryTime]);

  } else {
    logger.error(`Message ${message.job_id} exceeded max retries (${message.max_retries}), marking as FAILED`);

    await updateMessageStatus(message.id, 'FAILED', client, {
      failed_at: new Date(),
      error_message: errorMessage
    });
  }
}

async function markDeviceTokenInactive(deviceTokenId: number): Promise<void> {
  // This would update the device token in the database
  // For now, we'll log it - you should implement this based on your database connection setup
  console.warn(`Should mark device token ${deviceTokenId} as inactive`);
}