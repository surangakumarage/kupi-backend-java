import { ScheduledHandler } from 'aws-lambda';
import { getClient } from '../../services/database/connection';
import { Logger } from '../../utils/logger';

interface ScheduledTemplate {
  id: number;
  template_code: string;
  title: string;
  message_content: string;
  schedule_day_of_month: number;
  schedule_time: string;
  schedule_timezone: string;
  schedule_target_audience: string;
  variables: any[];
}

export const handler: ScheduledHandler = async (event, context) => {
  const logger = new Logger('ScheduledTemplateProcessor');
  let client;

  try {
    logger.info('Starting scheduled template processing...', {
      time: new Date().toISOString(),
      event: event
    });

    // Get database connection
    client = await getClient();

    // Get current date info
    const now = new Date();
    const currentDay = now.getDate();
    const currentHour = now.getUTCHours();
    const currentMinute = now.getUTCMinutes();

    logger.info(`Processing for day ${currentDay}, hour ${currentHour}, minute ${currentMinute}`);

    // Find templates that should be sent today
    const query = `
      SELECT
        nt.*,
        COALESCE(
          json_agg(
            json_build_object(
              'variable_name', tv.variable_name,
              'variable_type', tv.variable_type,
              'default_value', tv.default_value,
              'is_required', tv.is_required
            )
          ) FILTER (WHERE tv.id IS NOT NULL),
          '[]'
        ) as variables
      FROM notification_templates nt
      LEFT JOIN template_variables tv ON nt.id = tv.template_id
      WHERE nt.schedule_enabled = true
        AND nt.is_active = true
        AND nt.schedule_day_of_month = $1
        AND EXTRACT(HOUR FROM nt.schedule_time) = $2
        AND EXTRACT(MINUTE FROM nt.schedule_time) = $3
      GROUP BY nt.id
      ORDER BY nt.schedule_time
    `;

    const result = await client.query(query, [currentDay, currentHour, currentMinute]);
    const templatesToProcess = result.rows as ScheduledTemplate[];

    logger.info(`Found ${templatesToProcess.length} templates to process`);

    for (const template of templatesToProcess) {
      try {
        await processTemplate(template, logger, client);
      } catch (error) {
        logger.error(`Failed to process template ${template.template_code}:`, error);
        // Continue with other templates even if one fails
      }
    }

    logger.info('Scheduled template processing completed successfully');

    return {
      statusCode: 200,
      body: JSON.stringify({
        message: 'Scheduled template processing completed',
        processedTemplates: templatesToProcess.length,
        templates: templatesToProcess.map(t => ({
          template_code: t.template_code,
          title: t.title,
          target_audience: t.schedule_target_audience
        }))
      })
    };

  } catch (error: any) {
    logger.error('Failed to process scheduled templates:', error);
    throw error;
  } finally {
    if (client) {
      client.release();
    }
  }
};

async function processTemplate(
  template: ScheduledTemplate,
  logger: Logger,
  client: any
): Promise<void> {
  logger.info(`Processing template: ${template.template_code}`, {
    title: template.title,
    target_audience: template.schedule_target_audience,
    day: template.schedule_day_of_month,
    time: template.schedule_time
  });

  // Here you would implement the logic to:
  // 1. Get target users based on template.schedule_target_audience
  // 2. Process message with variables
  // 3. Send notifications (push, SMS, email, etc.)

  try {
    // Example: Get target users
    const targetUsers = await getTargetUsers(template.schedule_target_audience, client, logger);

    // Example: Process and send notifications
    for (const user of targetUsers) {
      const processedMessage = await processMessageVariables(
        template.message_content,
        template.variables,
        user
      );

      await sendNotification(user, processedMessage, template, logger);
    }

    // Log successful processing
    await logTemplateExecution(template, targetUsers.length, client, logger);

  } catch (error) {
    logger.error(`Error processing template ${template.template_code}:`, error);
    throw error;
  }
}

async function getTargetUsers(targetAudience: string, client: any, logger: Logger): Promise<any[]> {
  // Implement user targeting logic based on audience
  // Examples:
  // - "ALL" -> all active users
  // - "Free / Expired users" -> users without active subscriptions
  // - "Paid Subscribers" -> users with active subscriptions

  let query = '';
  let params: any[] = [];

  switch (targetAudience.toUpperCase()) {
    case 'ALL':
      query = `
        SELECT id, mobile_number, email, first_name, last_name
        FROM users
        WHERE is_active = true
          AND is_deleted = false
      `;
      break;

    case 'PAID SUBSCRIBERS':
      query = `
        SELECT u.id, u.mobile_number, u.email, u.first_name, u.last_name
        FROM users u
        INNER JOIN subscriptions s ON u.id = s.user_id
        WHERE u.is_active = true
          AND u.is_deleted = false
          AND s.status = 'active'
          AND s.end_date > CURRENT_DATE
      `;
      break;

    case 'ALL FREE / EXPIRED USERS':
      query = `
        SELECT u.id, u.mobile_number, u.email, u.first_name, u.last_name
        FROM users u
        LEFT JOIN subscriptions s ON u.id = s.user_id AND s.status = 'active' AND s.end_date > CURRENT_DATE
        WHERE u.is_active = true
          AND u.is_deleted = false
          AND s.id IS NULL
      `;
      break;

    default:
      logger.warn(`Unknown target audience: ${targetAudience}, defaulting to ALL`);
      query = `
        SELECT id, mobile_number, email, first_name, last_name
        FROM users
        WHERE is_active = true
          AND is_deleted = false
      `;
  }

  const result = await client.query(query, params);
  logger.info(`Found ${result.rows.length} target users for audience: ${targetAudience}`);

  return result.rows;
}

async function processMessageVariables(
  messageContent: string,
  variables: any[],
  user: any
): Promise<string> {
  // Replace variables in message content
  // This is a simplified version - you'd want more robust variable processing

  let processedMessage = messageContent;

  // Replace common user variables
  processedMessage = processedMessage.replace(/\$name/g, user.first_name || 'User');
  processedMessage = processedMessage.replace(/\$userName/g, `${user.first_name} ${user.last_name}`.trim() || 'User');

  // Process template-specific variables based on the variables array
  for (const variable of variables) {
    const placeholder = `$${variable.variable_name}`;

    if (processedMessage.includes(placeholder)) {
      // Get variable value (implement your business logic here)
      const value = await getVariableValue(variable.variable_name, user);
      processedMessage = processedMessage.replace(
        new RegExp(`\\$${variable.variable_name}`, 'g'),
        value || variable.default_value || ''
      );
    }
  }

  return processedMessage;
}

async function getVariableValue(variableName: string, user: any): Promise<string> {
  // Implement business logic to get variable values
  // Examples based on our template variables:

  switch (variableName) {
    case 'month':
      const monthNames = ['January', 'February', 'March', 'April', 'May', 'June',
        'July', 'August', 'September', 'October', 'November', 'December'];
      return monthNames[new Date().getMonth()];

    case 'totalSavings':
      // Calculate user's total savings (implement your logic)
      return 'LKR 2,500'; // Placeholder

    default:
      return '';
  }
}

async function sendNotification(
  user: any,
  message: string,
  template: ScheduledTemplate,
  logger: Logger
): Promise<void> {
  // Implement notification sending logic
  // This would call your notification service (push, SMS, email, etc.)

  logger.info(`Sending ${template.template_code} to user ${user.id}`, {
    message: message.substring(0, 100) + '...',
    user_id: user.id,
    template_code: template.template_code
  });

  // Example: Call notification service
  // await notificationService.sendPushNotification(user.id, message);
}

async function logTemplateExecution(
  template: ScheduledTemplate,
  userCount: number,
  client: any,
  logger: Logger
): Promise<void> {
  // Log the execution for tracking and analytics
  const logQuery = `
    INSERT INTO scheduled_template_executions
    (template_id, template_code, executed_at, target_user_count, status)
    VALUES ($1, $2, $3, $4, $5)
  `;

  try {
    await client.query(logQuery, [
      template.id,
      template.template_code,
      new Date(),
      userCount,
      'completed'
    ]);

    logger.info(`Logged execution for template ${template.template_code}`, {
      target_users: userCount,
      status: 'completed'
    });
  } catch (error) {
    // Don't fail the whole process if logging fails
    logger.warn(`Failed to log template execution: ${error}`);
  }
}