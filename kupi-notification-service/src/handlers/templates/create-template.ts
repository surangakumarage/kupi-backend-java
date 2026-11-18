import { APIGatewayProxyHandler, APIGatewayProxyEvent } from 'aws-lambda';
import { getDbClient } from '../../utils/database';
import { createdResponse, errorResponse, serverErrorResponse } from '../../utils/response';
import { validateCreateTemplate, ValidationError } from '../../utils/validation';
import { CreateTemplateRequest, TemplateWithVariables } from '../../types/template';

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

    // Parse request body
    if (!event.body) {
      return errorResponse(400, 'Request body is required');
    }

    let requestData: CreateTemplateRequest;
    try {
      requestData = JSON.parse(event.body);
    } catch (error) {
      return errorResponse(400, 'Invalid JSON in request body');
    }

    // Validate request data
    validateCreateTemplate(requestData);

    console.log(`[create-template] Creating template: ${requestData.template_code}`);

    // Get database connection
    client = await getDbClient();

    // Start transaction
    await client.query('BEGIN');

    try {
      // Check if template code already exists
      const existsQuery = `
        SELECT id FROM notification_templates
        WHERE LOWER(template_code) = LOWER($1)
      `;
      const existsResult = await client.query(existsQuery, [requestData.template_code]);

      if (existsResult.rows.length > 0) {
        throw new ValidationError(`Template code '${requestData.template_code}' already exists`);
      }

      // Get created_by from request context or default to 'ADMIN'
      // In a real implementation, this would come from JWT token
      const createdBy = 'ADMIN'; // TODO: Extract from authentication context

      // Insert template
      const insertTemplateQuery = `
        INSERT INTO notification_templates (
          template_code,
          title,
          description,
          message_content,
          template_type,
          category,
          is_active,
          version,
          created_by
        ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)
        RETURNING *
      `;

      const templateValues = [
        requestData.template_code,
        requestData.title,
        requestData.description || null,
        requestData.message_content,
        requestData.template_type,
        requestData.category,
        true, // is_active defaults to true
        1,    // version starts at 1
        createdBy,
      ];

      console.log(`[create-template] Inserting template with values: ${JSON.stringify(templateValues)}`);
      const templateResult = await client.query(insertTemplateQuery, templateValues);
      const newTemplate = templateResult.rows[0];

      console.log(`[create-template] Template created with ID: ${newTemplate.id}`);

      // Insert template variables if provided
      const variables = [];
      if (requestData.variables && requestData.variables.length > 0) {
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
          console.log(`[create-template] Inserting variable: ${variable.variable_name}`);
          const variableValues = [
            newTemplate.id,
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

      // Create initial version
      const insertVersionQuery = `
        INSERT INTO template_versions (
          template_id,
          version,
          message_content,
          variables_snapshot,
          created_by,
          is_active
        ) VALUES ($1, $2, $3, $4, $5, $6)
        RETURNING id
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
        newTemplate.id,
        1,
        newTemplate.message_content,
        variablesSnapshot,
        createdBy,
        true,
      ]);

      console.log(`[create-template] Initial version created for template: ${newTemplate.id}`);

      // Commit transaction
      await client.query('COMMIT');

      // Prepare response data
      const responseTemplate: TemplateWithVariables = {
        id: newTemplate.id,
        template_code: newTemplate.template_code,
        title: newTemplate.title,
        description: newTemplate.description,
        message_content: newTemplate.message_content,
        template_type: newTemplate.template_type,
        category: newTemplate.category,
        is_active: newTemplate.is_active,
        version: newTemplate.version,
        created_by: newTemplate.created_by,
        updated_by: newTemplate.updated_by,
        created_at: newTemplate.created_at,
        updated_at: newTemplate.updated_at,
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

      console.log(`[create-template] Template '${responseTemplate.template_code}' created successfully with ${variables.length} variables`);

      return createdResponse(
        responseTemplate,
        `Template '${responseTemplate.template_code}' created successfully`
      );

    } catch (error) {
      // Rollback transaction on error
      await client.query('ROLLBACK');
      throw error;
    }

  } catch (error: any) {
    console.error('[create-template] Error:', error);

    if (error.name === 'ValidationError') {
      return errorResponse(400, error.message);
    }

    // Check for unique constraint violation (duplicate template code)
    if (error.code === '23505' && error.constraint === 'unique_template_code') {
      return errorResponse(409, `Template code already exists`);
    }

    return serverErrorResponse('Failed to create template', error.message);
  } finally {
    if (client) {
      client.release();
    }
  }
};