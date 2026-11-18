import { APIGatewayProxyHandler, APIGatewayProxyEvent } from 'aws-lambda';
import { getDbClient } from '../../utils/database';
import { successResponse, notFoundResponse, errorResponse, serverErrorResponse } from '../../utils/response';
import { TemplateWithVariables } from '../../types/template';

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

    // Extract template ID from path parameters
    const templateId = event.pathParameters?.id;

    if (!templateId) {
      return errorResponse(400, 'Template ID is required');
    }

    // Validate template ID is a number
    const id = parseInt(templateId);
    if (isNaN(id) || id <= 0) {
      return errorResponse(400, 'Invalid template ID. Must be a positive integer');
    }

    console.log(`[get-template] Fetching template with ID: ${id}`);

    // Get database connection
    client = await getDbClient();

    // Query to get template with its variables
    const templateQuery = `
      SELECT
        id,
        template_code,
        title,
        description,
        message_content,
        template_type,
        category,
        is_active,
        version,
        created_by,
        updated_by,
        created_at,
        updated_at
      FROM notification_templates
      WHERE id = $1
    `;

    const variablesQuery = `
      SELECT
        id,
        template_id,
        variable_name,
        variable_type,
        default_value,
        is_required,
        description,
        created_at
      FROM template_variables
      WHERE template_id = $1
      ORDER BY variable_name
    `;

    // Execute both queries
    console.log(`[get-template] Executing template query for ID: ${id}`);
    const templateResult = await client.query(templateQuery, [id]);

    if (templateResult.rows.length === 0) {
      console.log(`[get-template] Template not found with ID: ${id}`);
      return notFoundResponse(`Template with ID ${id} not found`);
    }

    console.log(`[get-template] Executing variables query for template ID: ${id}`);
    const variablesResult = await client.query(variablesQuery, [id]);

    // Transform database results to API format
    const templateRow = templateResult.rows[0];
    const variableRows = variablesResult.rows;

    const template: TemplateWithVariables = {
      id: templateRow.id,
      template_code: templateRow.template_code,
      title: templateRow.title,
      description: templateRow.description,
      message_content: templateRow.message_content,
      template_type: templateRow.template_type,
      category: templateRow.category,
      is_active: templateRow.is_active,
      version: templateRow.version,
      created_by: templateRow.created_by,
      updated_by: templateRow.updated_by,
      created_at: templateRow.created_at,
      updated_at: templateRow.updated_at,
      variables: variableRows.map(row => ({
        id: row.id,
        template_id: row.template_id,
        variable_name: row.variable_name,
        variable_type: row.variable_type,
        default_value: row.default_value,
        is_required: row.is_required,
        description: row.description,
        created_at: row.created_at,
      })),
    };

    console.log(`[get-template] Retrieved template '${template.template_code}' with ${template.variables.length} variables`);

    return successResponse(template, `Template '${template.template_code}' retrieved successfully`);

  } catch (error: any) {
    console.error('[get-template] Error:', error);
    return serverErrorResponse('Failed to retrieve template', error.message);
  } finally {
    if (client) {
      client.release();
    }
  }
};