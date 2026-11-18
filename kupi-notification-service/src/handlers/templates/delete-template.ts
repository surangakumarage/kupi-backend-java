import { APIGatewayProxyHandler, APIGatewayProxyEvent } from 'aws-lambda';
import { getDbClient } from '../../utils/database';
import { successResponse, notFoundResponse, errorResponse, serverErrorResponse } from '../../utils/response';

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

    console.log(`[delete-template] Attempting to delete template with ID: ${id}`);

    // Get database connection
    client = await getDbClient();

    // Start transaction
    await client.query('BEGIN');

    try {
      // Check if template exists and get its details
      const existsQuery = `
        SELECT id, template_code, title FROM notification_templates
        WHERE id = $1
      `;
      const existsResult = await client.query(existsQuery, [id]);

      if (existsResult.rows.length === 0) {
        console.log(`[delete-template] Template not found with ID: ${id}`);
        return notFoundResponse(`Template with ID ${id} not found`);
      }

      const template = existsResult.rows[0];
      console.log(`[delete-template] Found template: ${template.template_code}`);

      // Check if template is currently being used (optional business logic)
      // You might want to prevent deletion of templates that are actively being used
      // For now, we'll allow deletion but log a warning

      // Delete template (this will cascade to variables and versions due to foreign key constraints)
      const deleteQuery = `
        DELETE FROM notification_templates
        WHERE id = $1
        RETURNING template_code, title
      `;

      const deleteResult = await client.query(deleteQuery, [id]);

      if (deleteResult.rows.length === 0) {
        // This shouldn't happen given our previous check, but just in case
        throw new Error('Template deletion failed unexpectedly');
      }

      const deletedTemplate = deleteResult.rows[0];

      // Commit transaction
      await client.query('COMMIT');

      console.log(`[delete-template] Template '${deletedTemplate.template_code}' deleted successfully`);

      return successResponse(
        {
          id,
          template_code: deletedTemplate.template_code,
          title: deletedTemplate.title,
          deleted_at: new Date().toISOString(),
        },
        `Template '${deletedTemplate.template_code}' deleted successfully`
      );

    } catch (error) {
      // Rollback transaction on error
      await client.query('ROLLBACK');
      throw error;
    }

  } catch (error: any) {
    console.error('[delete-template] Error:', error);

    // Check for foreign key constraint violations
    if (error.code === '23503') {
      return errorResponse(409, 'Cannot delete template: it may be referenced by other records');
    }

    return serverErrorResponse('Failed to delete template', error.message);
  } finally {
    if (client) {
      client.release();
    }
  }
};