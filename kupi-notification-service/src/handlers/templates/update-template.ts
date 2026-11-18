import { APIGatewayProxyHandler, APIGatewayProxyEvent } from 'aws-lambda';
import { getDbClient } from '../../utils/database';
import { successResponse, notFoundResponse, errorResponse, serverErrorResponse } from '../../utils/response';
import { validateUpdateTemplate, ValidationError } from '../../utils/validation';
import { UpdateTemplateRequest, TemplateWithVariables } from '../../types/template';

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

    // Parse request body
    if (!event.body) {
      return errorResponse(400, 'Request body is required');
    }

    let requestData: UpdateTemplateRequest;
    try {
      requestData = JSON.parse(event.body);
    } catch (error) {
      return errorResponse(400, 'Invalid JSON in request body');
    }

    // Validate request data
    validateUpdateTemplate(requestData);

    console.log(`[update-template] Updating template with ID: ${id}`);

    // Get database connection
    client = await getDbClient();

    // Start transaction
    await client.query('BEGIN');

    try {
      // Check if template exists
      const existsQuery = `
        SELECT id, version, template_code FROM notification_templates
        WHERE id = $1
      `;
      const existsResult = await client.query(existsQuery, [id]);

      if (existsResult.rows.length === 0) {
        throw new ValidationError(`Template with ID ${id} not found`);
      }

      const currentTemplate = existsResult.rows[0];
      const updatedBy = 'ADMIN'; // TODO: Extract from authentication context

      // Build dynamic update query
      const updateFields: string[] = [];
      const updateValues: any[] = [];
      let paramCount = 0;

      if (requestData.title !== undefined) {
        paramCount++;
        updateFields.push(`title = $${paramCount}`);
        updateValues.push(requestData.title);
      }

      if (requestData.description !== undefined) {
        paramCount++;
        updateFields.push(`description = $${paramCount}`);
        updateValues.push(requestData.description);
      }

      if (requestData.message_content !== undefined) {
        paramCount++;
        updateFields.push(`message_content = $${paramCount}`);
        updateValues.push(requestData.message_content);
      }

      if (requestData.template_type !== undefined) {
        paramCount++;
        updateFields.push(`template_type = $${paramCount}`);
        updateValues.push(requestData.template_type);
      }

      if (requestData.category !== undefined) {
        paramCount++;
        updateFields.push(`category = $${paramCount}`);
        updateValues.push(requestData.category);
      }

      if (requestData.is_active !== undefined) {
        paramCount++;
        updateFields.push(`is_active = $${paramCount}`);
        updateValues.push(requestData.is_active);
      }

      // Always update version and updated_by
      const newVersion = currentTemplate.version + 1;
      paramCount++;
      updateFields.push(`version = $${paramCount}`);
      updateValues.push(newVersion);

      paramCount++;
      updateFields.push(`updated_by = $${paramCount}`);
      updateValues.push(updatedBy);

      paramCount++;
      updateFields.push(`updated_at = CURRENT_TIMESTAMP`);

      // Add WHERE clause parameter
      paramCount++;
      updateValues.push(id);

      // Update template
      const updateQuery = `
        UPDATE notification_templates
        SET ${updateFields.join(', ')}
        WHERE id = $${paramCount}
        RETURNING *
      `;

      console.log(`[update-template] Executing update query with ${updateFields.length} fields`);
      const updateResult = await client.query(updateQuery, updateValues);
      const updatedTemplate = updateResult.rows[0];

      // Handle variable updates if provided
      let variables = [];
      if (requestData.variables !== undefined) {
        // Delete existing variables
        await client.query('DELETE FROM template_variables WHERE template_id = $1', [id]);

        // Insert new variables
        if (requestData.variables.length > 0) {
          const insertVariableQuery = `
            INSERT INTO template_variables (
              template_id,
              variable_name,
              variable_type,
              default_value,
              is_required,
              description
            ) VALUES ($1, $2, $3, $4, $5, $6)
            RETURNING *
          `;

          for (const variable of requestData.variables) {
            console.log(`[update-template] Inserting/updating variable: ${variable.variable_name}`);
            const variableValues = [
              id,
              variable.variable_name,
              variable.variable_type,
              variable.default_value || null,
              variable.is_required || false,
              variable.description || null,
            ];

            const variableResult = await client.query(insertVariableQuery, variableValues);
            variables.push(variableResult.rows[0]);
          }
        }
      } else {
        // Get existing variables if not updating them
        const variablesQuery = `
          SELECT * FROM template_variables
          WHERE template_id = $1
          ORDER BY variable_name
        `;
        const variablesResult = await client.query(variablesQuery, [id]);
        variables = variablesResult.rows;
      }

      // Create new version if content changed
      if (requestData.message_content !== undefined || requestData.variables !== undefined) {
        const insertVersionQuery = `
          INSERT INTO template_versions (
            template_id,
            version,
            message_content,
            variables_snapshot,
            created_by,
            is_active
          ) VALUES ($1, $2, $3, $4, $5, $6)
        `;

        const variablesSnapshot = variables.length > 0 ? JSON.stringify(
          variables.map(v => ({
            name: v.variable_name,
            type: v.variable_type,
            required: v.is_required,
            default: v.default_value,
            description: v.description,
          }))
        ) : null;

        await client.query(insertVersionQuery, [
          id,
          newVersion,
          updatedTemplate.message_content,
          variablesSnapshot,
          updatedBy,
          true,
        ]);

        // Mark previous versions as inactive
        await client.query(
          'UPDATE template_versions SET is_active = false WHERE template_id = $1 AND version < $2',
          [id, newVersion]
        );

        console.log(`[update-template] Created version ${newVersion} for template: ${id}`);
      }

      // Commit transaction
      await client.query('COMMIT');

      // Prepare response data
      const responseTemplate: TemplateWithVariables = {
        id: updatedTemplate.id,
        template_code: updatedTemplate.template_code,
        title: updatedTemplate.title,
        description: updatedTemplate.description,
        message_content: updatedTemplate.message_content,
        template_type: updatedTemplate.template_type,
        category: updatedTemplate.category,
        is_active: updatedTemplate.is_active,
        version: updatedTemplate.version,
        created_by: updatedTemplate.created_by,
        updated_by: updatedTemplate.updated_by,
        created_at: updatedTemplate.created_at,
        updated_at: updatedTemplate.updated_at,
        variables: variables.map(v => ({
          id: v.id,
          template_id: v.template_id,
          variable_name: v.variable_name,
          variable_type: v.variable_type,
          default_value: v.default_value,
          is_required: v.is_required,
          description: v.description,
          created_at: v.created_at,
        })),
      };

      console.log(`[update-template] Template '${responseTemplate.template_code}' updated successfully to version ${responseTemplate.version}`);

      return successResponse(
        responseTemplate,
        `Template '${responseTemplate.template_code}' updated successfully`
      );

    } catch (error) {
      // Rollback transaction on error
      await client.query('ROLLBACK');
      throw error;
    }

  } catch (error: any) {
    console.error('[update-template] Error:', error);

    if (error.name === 'ValidationError') {
      return errorResponse(400, error.message);
    }

    return serverErrorResponse('Failed to update template', error.message);
  } finally {
    if (client) {
      client.release();
    }
  }
};