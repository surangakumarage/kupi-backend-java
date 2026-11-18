import { APIGatewayProxyHandler, APIGatewayProxyEvent } from 'aws-lambda';
import { getDbClient } from '../../utils/database';
import { successResponse, errorResponse, serverErrorResponse, notFoundResponse } from '../../utils/response';
import { validateVariableSubstitution, ValidationError } from '../../utils/validation';
import { substituteVariables } from '../../utils/variable-substitution';
import { requireAdmin } from '../../utils/auth';
import { TemplateType } from '../../types/template';

interface SendNotificationRequest {
  template_code: string;
  variables: Record<string, any>;
  recipient: string;
  template_type?: TemplateType;
  dry_run?: boolean;
}

export const handler: APIGatewayProxyHandler = async (event: APIGatewayProxyEvent) => {
  let client;

  try {
    // Handle CORS preflight
    if (event.httpMethod === 'OPTIONS') {
      return {
        statusCode: 200,
        headers: {
          'Access-Control-Allow-Origin': '*',
          'Access-Control-Allow-Headers': 'Content-Type, Authorization, X-Amz-Date, X-Api-Key, X-Amz-Security-Token, X-Amz-User-Agent, Accept, Accept-Language, Time-Zone',
          'Access-Control-Allow-Methods': 'GET, HEAD, OPTIONS, POST, PUT, DELETE, PATCH',
        },
        body: '',
      };
    }

    // ⚠️ ADMIN ONLY ACCESS: Verify user is an administrator
    const authContext = await requireAdmin(event);

    // Parse request body
    if (!event.body) {
      return errorResponse(400, 'Request body is required');
    }

    let requestData: SendNotificationRequest;
    try {
      requestData = JSON.parse(event.body);
    } catch (error) {
      return errorResponse(400, 'Invalid JSON in request body');
    }

    // Validate required fields
    if (!requestData.template_code) {
      return errorResponse(400, 'Template code is required');
    }

    if (!requestData.variables || typeof requestData.variables !== 'object') {
      return errorResponse(400, 'Variables object is required');
    }

    if (!requestData.recipient) {
      return errorResponse(400, 'Recipient is required');
    }

    const { template_code, variables, recipient, template_type, dry_run = false } = requestData;

    console.log(`[send-notification] Admin ${authContext.email} sending notification using template: ${template_code}, Recipient: ${recipient}, Dry run: ${dry_run}`);

    // Get database connection
    client = await getDbClient();

    // Get template with variables
    const templateQuery = `
      SELECT
        id,
        template_code,
        title,
        message_content,
        template_type,
        is_active
      FROM notification_templates
      WHERE LOWER(template_code) = LOWER($1) AND is_active = true
    `;

    const variablesQuery = `
      SELECT
        variable_name,
        variable_type,
        default_value,
        is_required,
        description
      FROM template_variables
      WHERE template_id = $1
      ORDER BY variable_name
    `;

    // Get template
    console.log(`[send-notification] Fetching template: ${template_code}`);
    const templateResult = await client.query(templateQuery, [template_code]);

    if (templateResult.rows.length === 0) {
      console.log(`[send-notification] Template not found or inactive: ${template_code}`);
      return notFoundResponse(`Template '${template_code}' not found or is inactive`);
    }

    const template = templateResult.rows[0];
    console.log(`[send-notification] Found template: ${template.title} (${template.template_type})`);

    // Validate template type if provided
    if (template_type && template_type !== template.template_type) {
      return errorResponse(400, `Template type mismatch. Template '${template_code}' is of type '${template.template_type}', but '${template_type}' was requested`);
    }

    // Get template variables
    const variablesResult = await client.query(variablesQuery, [template.id]);
    const templateVariables = variablesResult.rows;

    console.log(`[send-notification] Template has ${templateVariables.length} defined variables`);

    // Validate required variables
    const requiredVariables = templateVariables
      .filter(tv => tv.is_required)
      .map(tv => tv.variable_name);

    validateVariableSubstitution(template.message_content, variables, requiredVariables);

    // Process message with variable substitution
    const result = substituteVariables(template.message_content, variables, templateVariables);
    const processedMessage = result.processedMessage;

    console.log(`[send-notification] Message processed successfully: ${processedMessage.length} characters`);

    // Validate recipient based on template type
    let recipientValidation = validateRecipient(recipient, template.template_type as TemplateType);
    if (!recipientValidation.isValid) {
      return errorResponse(400, recipientValidation.error!);
    }

    // Prepare notification data
    const notificationData = {
      template_code: template.template_code,
      template_title: template.title,
      template_type: template.template_type,
      recipient: recipient,
      processed_message: processedMessage,
      variables_used: result.variablesUsed,
      sent_by: authContext.email,
      sent_at: new Date().toISOString(),
      dry_run,
    };

    if (dry_run) {
      console.log(`[send-notification] Dry run completed - no notification sent`);

      return successResponse(
        {
          ...notificationData,
          status: 'DRY_RUN',
          message: 'Notification prepared successfully (dry run mode)',
        },
        `Notification prepared for template '${template_code}' (dry run mode)`
      );
    }

    // TODO: Implement actual notification sending based on template type
    // This would integrate with SMS, Email, Push, or WhatsApp services
    const sendResult = await sendNotificationByType(
      template.template_type as TemplateType,
      recipient,
      processedMessage,
      template.title
    );

    console.log(`[send-notification] Notification sent successfully: ${sendResult.messageId}`);

    return successResponse(
      {
        ...notificationData,
        status: 'SENT',
        message_id: sendResult.messageId,
        delivery_status: sendResult.status,
        provider_response: sendResult.providerResponse,
      },
      `Notification sent successfully using template '${template_code}'`
    );

  } catch (error: any) {
    console.error('[send-notification] Error:', error);

    // Handle authentication errors
    if (error.statusCode && error.response) {
      return error.response;
    }

    if (error.name === 'ValidationError') {
      return errorResponse(400, error.message);
    }

    return serverErrorResponse('Failed to send notification', error.message);
  } finally {
    if (client) {
      client.release();
    }
  }
};

/**
 * Validate recipient format based on template type
 */
function validateRecipient(recipient: string, templateType: TemplateType): { isValid: boolean; error?: string } {
  switch (templateType) {
    case TemplateType.SMS:
    case TemplateType.WHATSAPP:
      // Phone number validation - should start with + and contain only digits
      const phoneRegex = /^\+[1-9]\d{1,14}$/;
      if (!phoneRegex.test(recipient)) {
        return {
          isValid: false,
          error: 'Invalid phone number format. Must be in international format (e.g., +1234567890)'
        };
      }
      break;

    case TemplateType.EMAIL:
      // Email validation
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(recipient)) {
        return {
          isValid: false,
          error: 'Invalid email address format'
        };
      }
      break;

    case TemplateType.PUSH:
      // Push notification token validation (basic check)
      if (!recipient || recipient.length < 10) {
        return {
          isValid: false,
          error: 'Invalid push notification token'
        };
      }
      break;

    default:
      return {
        isValid: false,
        error: `Unsupported template type: ${templateType}`
      };
  }

  return { isValid: true };
}

/**
 * Send notification based on template type
 * TODO: Implement actual notification providers
 */
async function sendNotificationByType(
  templateType: TemplateType,
  recipient: string,
  message: string,
  subject?: string
): Promise<{ messageId: string; status: string; providerResponse?: any }> {
  // Generate a mock message ID for now
  const messageId = `msg_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;

  console.log(`[sendNotificationByType] Sending ${templateType} notification to ${recipient}`);

  switch (templateType) {
    case TemplateType.SMS:
      // TODO: Integrate with SMS service (AWS SNS, Twilio, etc.)
      console.log(`[SMS] Would send to ${recipient}: ${message}`);
      return {
        messageId,
        status: 'QUEUED',
        providerResponse: { provider: 'SMS_MOCK', status: 'queued' }
      };

    case TemplateType.EMAIL:
      // TODO: Integrate with email service (AWS SES, SendGrid, etc.)
      console.log(`[EMAIL] Would send to ${recipient} with subject "${subject}": ${message}`);
      return {
        messageId,
        status: 'QUEUED',
        providerResponse: { provider: 'EMAIL_MOCK', status: 'queued' }
      };

    case TemplateType.PUSH:
      // TODO: Integrate with push notification service (FCM, APNS, etc.)
      console.log(`[PUSH] Would send to ${recipient}: ${message}`);
      return {
        messageId,
        status: 'QUEUED',
        providerResponse: { provider: 'PUSH_MOCK', status: 'queued' }
      };

    case TemplateType.WHATSAPP:
      // TODO: Integrate with WhatsApp Business API
      console.log(`[WHATSAPP] Would send to ${recipient}: ${message}`);
      return {
        messageId,
        status: 'QUEUED',
        providerResponse: { provider: 'WHATSAPP_MOCK', status: 'queued' }
      };

    default:
      throw new Error(`Unsupported template type: ${templateType}`);
  }
}