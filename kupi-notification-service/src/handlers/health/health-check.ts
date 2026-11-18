import { APIGatewayProxyHandler, APIGatewayProxyEvent } from 'aws-lambda';
import { getDbClient } from '../../utils/database';
import { successResponse, serverErrorResponse } from '../../utils/response';

export const handler: APIGatewayProxyHandler = async (event: APIGatewayProxyEvent) => {
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

    const startTime = Date.now();
    let dbStatus = 'unknown';
    let dbError: string | undefined;

    // Test database connection
    let client;
    try {
      client = await getDbClient();

      // Simple query to test database connectivity
      const result = await client.query('SELECT 1 as health_check');
      if (result.rows.length > 0 && result.rows[0].health_check === 1) {
        dbStatus = 'healthy';
      } else {
        dbStatus = 'unhealthy';
        dbError = 'Unexpected query result';
      }
    } catch (error: any) {
      dbStatus = 'unhealthy';
      dbError = error.message;
    } finally {
      if (client) {
        client.release();
      }
    }

    const responseTime = Date.now() - startTime;
    const timestamp = new Date().toISOString();

    // Check if service is overall healthy
    const isHealthy = dbStatus === 'healthy';

    const healthData = {
      service: 'kupi-notification-service',
      status: isHealthy ? 'healthy' : 'unhealthy',
      timestamp,
      response_time_ms: responseTime,
      version: process.env.APP_VERSION || '1.0.0',
      environment: process.env.NODE_ENV || 'development',
      components: {
        database: {
          status: dbStatus,
          error: dbError,
        },
        memory: {
          used: process.memoryUsage().heapUsed,
          total: process.memoryUsage().heapTotal,
          external: process.memoryUsage().external,
        },
        uptime_seconds: process.uptime(),
      },
      features: {
        template_management: 'enabled',
        variable_substitution: 'enabled',
        admin_authentication: 'enabled',
        notification_sending: 'mock_mode', // TODO: Change when real providers are implemented
      },
    };

    console.log(`[health-check] Service health: ${healthData.status}, DB: ${dbStatus}, Response time: ${responseTime}ms`);

    // Return appropriate status code
    const statusCode = isHealthy ? 200 : 503;

    return {
      statusCode,
      headers: {
        'Content-Type': 'application/json',
        'Access-Control-Allow-Origin': '*',
        'Cache-Control': 'no-cache, no-store, must-revalidate',
      },
      body: JSON.stringify({
        status: isHealthy ? 'success' : 'error',
        message: isHealthy ? 'Service is healthy' : 'Service is unhealthy',
        data: healthData,
      }),
    };

  } catch (error: any) {
    console.error('[health-check] Error:', error);

    const errorData = {
      service: 'kupi-notification-service',
      status: 'error',
      timestamp: new Date().toISOString(),
      error: error.message,
      version: process.env.APP_VERSION || '1.0.0',
      environment: process.env.NODE_ENV || 'development',
    };

    return {
      statusCode: 500,
      headers: {
        'Content-Type': 'application/json',
        'Access-Control-Allow-Origin': '*',
        'Cache-Control': 'no-cache, no-store, must-revalidate',
      },
      body: JSON.stringify({
        status: 'error',
        message: 'Health check failed',
        data: errorData,
      }),
    };
  }
};