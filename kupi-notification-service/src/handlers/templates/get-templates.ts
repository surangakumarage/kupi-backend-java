import { APIGatewayProxyHandler, APIGatewayProxyEvent } from 'aws-lambda';
import { getDbClient } from '../../utils/database';
import { successResponse, errorResponse, serverErrorResponse } from '../../utils/response';
import { requireAdmin } from '../../utils/auth';

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

    // Extract query parameters
    const queryParams = event.queryStringParameters || {};
    const page = parseInt(queryParams.page || '1');
    const size = Math.min(parseInt(queryParams.size || '10'), 100); // Max 100 per page
    const search = queryParams.search;
    const category = queryParams.category;
    const template_type = queryParams.template_type;
    const is_active = queryParams.is_active;

    console.log(`[get-templates] Admin ${authContext.email} fetching templates - Page: ${page}, Size: ${size}, Search: ${search || 'None'}, Category: ${category || 'All'}, Type: ${template_type || 'All'}, Active: ${is_active || 'All'}`);

    // Get database connection
    client = await getDbClient();

    // Build dynamic SQL query with filters
    let baseQuery = `
      SELECT
        t.id,
        t.template_code,
        t.title,
        t.description,
        t.message_content,
        t.template_type,
        t.category,
        t.is_active,
        t.version,
        t.created_by,
        t.updated_by,
        t.created_at,
        t.updated_at,
        COUNT(v.id) as variable_count
      FROM notification_templates t
      LEFT JOIN template_variables v ON t.id = v.template_id
    `;

    const conditions: string[] = [];
    const params: any[] = [];
    let paramCount = 0;

    if (search) {
      paramCount++;
      conditions.push(`(LOWER(t.template_code) LIKE $${paramCount} OR LOWER(t.title) LIKE $${paramCount})`);
      params.push(`%${search.toLowerCase()}%`);
    }

    if (category) {
      paramCount++;
      conditions.push(`LOWER(t.category) = $${paramCount}`);
      params.push(category.toLowerCase());
    }

    if (template_type) {
      paramCount++;
      conditions.push(`t.template_type = $${paramCount}`);
      params.push(template_type);
    }

    if (is_active !== undefined) {
      paramCount++;
      conditions.push(`t.is_active = $${paramCount}`);
      params.push(is_active === 'true');
    }

    if (conditions.length > 0) {
      baseQuery += ` WHERE ${conditions.join(' AND ')}`;
    }

    baseQuery += ` GROUP BY t.id ORDER BY t.created_at DESC`;

    // Add pagination
    const offset = (page - 1) * size;
    paramCount++;
    baseQuery += ` LIMIT $${paramCount}`;
    params.push(size);

    paramCount++;
    baseQuery += ` OFFSET $${paramCount}`;
    params.push(offset);

    // Execute main query
    console.log(`[get-templates] Executing templates query`);
    const result = await client.query(baseQuery, params);

    // Count total (simplified for now)
    const countResult = await client.query(
      `SELECT COUNT(*) as total FROM notification_templates t ${
        conditions.length > 0 ? `WHERE ${conditions.join(' AND ')}` : ''
      }`,
      params.slice(0, -2) // Remove LIMIT and OFFSET params
    );

    const total = parseInt(countResult.rows[0]?.total || '0');
    const totalPages = Math.ceil(total / size);

    const response = {
      data: result.rows.map((row: any) => ({
        id: row.id,
        template_code: row.template_code,
        title: row.title,
        description: row.description,
        message_content: row.message_content,
        template_type: row.template_type,
        category: row.category,
        is_active: row.is_active,
        version: row.version,
        created_by: row.created_by,
        updated_by: row.updated_by,
        created_at: row.created_at,
        updated_at: row.updated_at,
        variable_count: parseInt(row.variable_count || '0'),
      })),
      pagination: {
        page,
        size,
        total,
        totalPages,
        hasNext: page < totalPages,
        hasPrev: page > 1,
      },
    };

    console.log(`[get-templates] Retrieved ${result.rows.length} templates (page ${page} of ${totalPages})`);
    return successResponse(response, 'Templates retrieved successfully');

  } catch (error: any) {
    console.error('[get-templates] Error:', error);

    // Handle authentication errors
    if (error.statusCode && error.response) {
      return error.response;
    }

    return serverErrorResponse('Failed to retrieve templates', error.message);
  } finally {
    if (client) {
      client.release();
    }
  }
};