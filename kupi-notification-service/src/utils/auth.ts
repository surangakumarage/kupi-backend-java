import jwt from 'jsonwebtoken';
import jwkToPem from 'jwk-to-pem';
import axios from 'axios';
import { APIGatewayProxyEvent } from 'aws-lambda';
import { unauthorizedResponse } from './response';

export interface DecodedToken {
  sub: string;
  email: string;
  email_verified: boolean;
  'custom:userType'?: string;
  'custom:role'?: string;
  aud: string;
  event_id: string;
  token_use: 'id';
  auth_time: number;
  iss: string;
  exp: number;
  iat: number;
}

export interface AuthContext {
  userId: string;
  email: string;
  userType: string;
  role: string;
  isAdmin: boolean;
}

let jwks: any = null;
let jwksExpiry: number = 0;

/**
 * Get JWKs from Cognito with caching
 */
const getJWKS = async (): Promise<any> => {
  const now = Date.now();

  // Return cached JWKs if still valid (cache for 1 hour)
  if (jwks && now < jwksExpiry) {
    return jwks;
  }

  const cognitoRegion = process.env.COGNITO_REGION || 'us-east-1';
  const userPoolId = process.env.COGNITO_USER_POOL_ID;

  if (!userPoolId) {
    throw new Error('COGNITO_USER_POOL_ID environment variable not set');
  }

  const jwksUrl = `https://cognito-idp.${cognitoRegion}.amazonaws.com/${userPoolId}/.well-known/jwks.json`;

  try {
    console.log(`[auth] Fetching JWKs from: ${jwksUrl}`);
    const response = await axios.get(jwksUrl, { timeout: 5000 });

    jwks = response.data;
    jwksExpiry = now + (60 * 60 * 1000); // Cache for 1 hour

    console.log(`[auth] JWKs fetched and cached successfully`);
    return jwks;
  } catch (error: any) {
    console.error('[auth] Failed to fetch JWKs:', error.message);
    throw new Error(`Failed to fetch JWKs: ${error.message}`);
  }
};

/**
 * Extract JWT token from Authorization header
 */
export const extractToken = (event: APIGatewayProxyEvent): string | null => {
  const authHeader = event.headers?.Authorization || event.headers?.authorization;

  if (!authHeader) {
    return null;
  }

  // Support both "Bearer <token>" and just "<token>"
  if (authHeader.startsWith('Bearer ')) {
    return authHeader.substring(7);
  }

  return authHeader;
};

/**
 * Verify JWT token using Cognito JWKs
 */
export const verifyToken = async (token: string): Promise<DecodedToken> => {
  try {
    // Decode token header to get kid
    const decodedHeader = jwt.decode(token, { complete: true });

    if (!decodedHeader || typeof decodedHeader === 'string') {
      throw new Error('Invalid token format');
    }

    const kid = decodedHeader.header.kid;

    if (!kid) {
      throw new Error('Token missing kid in header');
    }

    // Get JWKs and find the matching key
    const jwksData = await getJWKS();
    const jwk = jwksData.keys.find((key: any) => key.kid === kid);

    if (!jwk) {
      throw new Error(`JWK not found for kid: ${kid}`);
    }

    // Convert JWK to PEM
    const pem = jwkToPem(jwk);

    // Verify and decode the token
    const decoded = jwt.verify(token, pem, {
      algorithms: ['RS256'],
      issuer: `https://cognito-idp.${process.env.COGNITO_REGION || 'us-east-1'}.amazonaws.com/${process.env.COGNITO_USER_POOL_ID}`,
    }) as DecodedToken;

    console.log(`[auth] Token verified successfully for user: ${decoded.email}`);
    return decoded;

  } catch (error: any) {
    console.error('[auth] Token verification failed:', error.message);
    throw new Error(`Token verification failed: ${error.message}`);
  }
};

/**
 * Extract auth context from decoded token
 */
export const getAuthContext = (decoded: DecodedToken): AuthContext => {
  const userType = decoded['custom:userType'] || decoded['custom:role'] || 'USER';
  const role = decoded['custom:role'] || userType;

  return {
    userId: decoded.sub,
    email: decoded.email,
    userType: userType.toUpperCase(),
    role: role.toUpperCase(),
    isAdmin: userType.toUpperCase() === 'ADMIN' || role.toUpperCase() === 'ADMIN',
  };
};

/**
 * Middleware function to validate authentication and require admin role
 */
export const requireAdmin = async (event: APIGatewayProxyEvent) => {
  // Extract token from request
  const token = extractToken(event);

  if (!token) {
    console.log('[auth] No authorization token provided');
    throw {
      statusCode: 401,
      response: unauthorizedResponse('Authorization token is required')
    };
  }

  try {
    // Verify token
    const decoded = await verifyToken(token);

    // Get auth context
    const authContext = getAuthContext(decoded);

    // Check if user is admin
    if (!authContext.isAdmin) {
      console.log(`[auth] Access denied for user ${authContext.email}: not an admin (role: ${authContext.role})`);
      throw {
        statusCode: 403,
        response: unauthorizedResponse('Admin access required. This feature is only available to administrators.')
      };
    }

    console.log(`[auth] Admin access granted for user: ${authContext.email}`);
    return authContext;

  } catch (error: any) {
    // If it's already a structured error with statusCode, re-throw it
    if (error.statusCode) {
      throw error;
    }

    console.error('[auth] Authentication error:', error.message);
    throw {
      statusCode: 401,
      response: unauthorizedResponse(`Authentication failed: ${error.message}`)
    };
  }
};

/**
 * Optional middleware for endpoints that work for both admin and merchant
 * but may have different behavior
 */
export const requireAuth = async (event: APIGatewayProxyEvent): Promise<AuthContext> => {
  const token = extractToken(event);

  if (!token) {
    console.log('[auth] No authorization token provided');
    throw {
      statusCode: 401,
      response: unauthorizedResponse('Authorization token is required')
    };
  }

  try {
    const decoded = await verifyToken(token);
    const authContext = getAuthContext(decoded);

    console.log(`[auth] Access granted for user: ${authContext.email} (role: ${authContext.role})`);
    return authContext;

  } catch (error: any) {
    console.error('[auth] Authentication error:', error.message);
    throw {
      statusCode: 401,
      response: unauthorizedResponse(`Authentication failed: ${error.message}`)
    };
  }
};