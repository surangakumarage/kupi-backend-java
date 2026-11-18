import { APIGatewayProxyResult } from 'aws-lambda';

export interface ApiResponse<T = any> {
  status: string;
  message: string;
  data?: T;
  error?: string;
  pagination?: {
    currentPage: number;
    totalPages: number;
    totalItems: number;
    pageSize: number;
  };
}

export const createResponse = (
  statusCode: number,
  body: ApiResponse,
  headers: Record<string, string> = {}
): APIGatewayProxyResult => {
  return {
    statusCode,
    headers: {
      'Content-Type': 'application/json',
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Headers': 'Content-Type, Authorization, X-Amz-Date, X-Api-Key, X-Amz-Security-Token, X-Amz-User-Agent, Accept, Accept-Language, Time-Zone',
      'Access-Control-Allow-Methods': 'GET, HEAD, OPTIONS, POST, PUT, DELETE, PATCH',
      ...headers,
    },
    body: JSON.stringify(body),
  };
};

export const successResponse = <T = any>(
  data: T,
  message: string = 'Operation successful',
  pagination?: ApiResponse['pagination']
): APIGatewayProxyResult => {
  return createResponse(200, {
    status: 'success',
    message,
    data,
    pagination,
  });
};

export const createdResponse = <T = any>(
  data: T,
  message: string = 'Resource created successfully'
): APIGatewayProxyResult => {
  return createResponse(201, {
    status: 'success',
    message,
    data,
  });
};

export const errorResponse = (
  statusCode: number,
  message: string,
  error?: string
): APIGatewayProxyResult => {
  return createResponse(statusCode, {
    status: 'error',
    message,
    error,
  });
};

export const validationErrorResponse = (
  message: string,
  error?: string
): APIGatewayProxyResult => {
  return errorResponse(400, message, error);
};

export const notFoundResponse = (
  message: string = 'Resource not found'
): APIGatewayProxyResult => {
  return errorResponse(404, message);
};

export const serverErrorResponse = (
  message: string = 'Internal server error',
  error?: string
): APIGatewayProxyResult => {
  return errorResponse(500, message, error);
};

export const unauthorizedResponse = (
  message: string = 'Unauthorized'
): APIGatewayProxyResult => {
  return errorResponse(401, message);
};