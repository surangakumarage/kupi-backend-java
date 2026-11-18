import { APIGatewayProxyHandler, APIGatewayProxyEvent } from 'aws-lambda';
import { getDbClient } from '../../utils/database';
import { successResponse, errorResponse, serverErrorResponse, notFoundResponse } from '../../utils/response';
import { validateVariableSubstitution, ValidationError } from '../../utils/validation';
import { substituteVariables, previewMessageSubstitution } from '../../utils/variable-substitution';
import { requireAdmin } from '../../utils/auth';

interface ProcessMessageRequest {
  template_code: string;
  variables: Record<string, any>;
  preview_mode?: boolean;
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

    let requestData: ProcessMessageRequest;
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

    const { template_code, variables, preview_mode = false } = requestData;

    console.log(`[process-message] Admin ${authContext.email} processing template: ${template_code}, Preview mode: ${preview_mode}`);

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
    console.log(`[process-message] Fetching template: ${template_code}`);
    const templateResult = await client.query(templateQuery, [template_code]);

    if (templateResult.rows.length === 0) {
      console.log(`[process-message] Template not found or inactive: ${template_code}`);
      return notFoundResponse(`Template '${template_code}' not found or is inactive`);
    }

    const template = templateResult.rows[0];
    console.log(`[process-message] Found template: ${template.title} (${template.template_type})`);

    // Get template variables
    const variablesResult = await client.query(variablesQuery, [template.id]);
    const templateVariables = variablesResult.rows;

    console.log(`[process-message] Template has ${templateVariables.length} defined variables`);

    // Process message with variable substitution
    let processedMessage: string;
    let variablesUsed: Record<string, any>;
    let missingVariables: string[] = [];

    try {
      if (preview_mode) {
        // In preview mode, don't throw errors for missing variables
        const result = previewMessageSubstitution(template.message_content, variables, templateVariables);
        processedMessage = result.processedMessage;
        variablesUsed = result.variablesUsed;
        missingVariables = result.missingVariables;

        console.log(`[process-message] Preview completed with ${missingVariables.length} missing variables`);
      } else {
        // Validate required variables
        const requiredVariables = templateVariables
          .filter(tv => tv.is_required)
          .map(tv => tv.variable_name);

        validateVariableSubstitution(template.message_content, variables, requiredVariables);

        // Process message
        const result = substituteVariables(template.message_content, variables, templateVariables);
        processedMessage = result.processedMessage;
        variablesUsed = result.variablesUsed;
        missingVariables = result.missingVariables;

        console.log(`[process-message] Processing completed successfully`);
      }

      // Prepare response
      const responseData = {
        template_code: template.template_code,
        template_title: template.title,
        template_type: template.template_type,
        original_message: template.message_content,
        processed_message: processedMessage,
        variables_used: variablesUsed,
        missing_variables: missingVariables,
        template_variables: templateVariables.map(tv => ({
          name: tv.variable_name,
          type: tv.variable_type,
          required: tv.is_required,
          default_value: tv.default_value,
          description: tv.description,
        })),
        preview_mode,
        processed_by: authContext.email,
        processed_at: new Date().toISOString(),
      };

      return successResponse(
        responseData,
        preview_mode
          ? `Template '${template_code}' previewed successfully`
          : `Template '${template_code}' processed successfully`
      );

    } catch (error: any) {
      if (error.name === 'ValidationError') {
        console.log(`[process-message] Validation error: ${error.message}`);
        return errorResponse(400, error.message);
      }
      throw error; // Re-throw for general error handling
    }

  } catch (error: any) {
    console.error('[process-message] Error:', error);

    // Handle authentication errors
    if (error.statusCode && error.response) {
      return error.response;
    }

    if (error.name === 'ValidationError') {
      return errorResponse(400, error.message);
    }

    return serverErrorResponse('Failed to process message template', error.message);
  } finally {
    if (client) {
      client.release();
    }
  }
};