import * as admin from 'firebase-admin';
import { Logger } from '../../utils/logger';

export interface PushNotificationPayload {
  notification: {
    title: string;
    body: string;
    icon?: string;
    sound?: string;
    click_action?: string;
  };
  data?: { [key: string]: string };
  android?: {
    priority?: 'normal' | 'high';
    notification?: {
      channel_id?: string;
      color?: string;
      tag?: string;
      icon?: string;
      sound?: string;
    };
  };
  apns?: {
    headers?: { [key: string]: string };
    payload?: {
      aps?: {
        badge?: number;
        sound?: string;
        category?: string;
      };
    };
  };
}

export interface SendNotificationResult {
  success: boolean;
  messageId?: string;
  error?: string;
  platform?: string;
}

export class FirebaseService {
  private logger: Logger;
  private initialized: boolean = false;

  constructor() {
    this.logger = new Logger('FirebaseService');
  }

  private initializeFirebase(): void {
    if (this.initialized || admin.apps.length > 0) {
      return;
    }

    try {
      const projectId = process.env.FCM_PROJECT_ID;
      const privateKey = process.env.FCM_PRIVATE_KEY?.replace(/\\n/g, '\n');
      const clientEmail = process.env.FCM_CLIENT_EMAIL;

      if (!projectId || !privateKey || !clientEmail) {
        throw new Error('Firebase configuration missing. Please set FCM_PROJECT_ID, FCM_PRIVATE_KEY, and FCM_CLIENT_EMAIL environment variables.');
      }

      admin.initializeApp({
        credential: admin.credential.cert({
          projectId,
          privateKey,
          clientEmail,
        })
      });

      this.initialized = true;
      this.logger.info('Firebase Admin SDK initialized successfully');

    } catch (error) {
      this.logger.error('Failed to initialize Firebase Admin SDK:', error);
      throw error;
    }
  }

  async sendToDevice(
    deviceToken: string,
    payload: PushNotificationPayload,
    platform?: string
  ): Promise<SendNotificationResult> {

    try {
      this.initializeFirebase();

      const message = {
        token: deviceToken,
        notification: payload.notification,
        data: payload.data || {},
        android: payload.android,
        apns: payload.apns
      };

      this.logger.debug('Sending push notification to device', {
        platform,
        tokenPrefix: deviceToken.substring(0, 20) + '...',
        title: payload.notification.title
      });

      const messageId = await admin.messaging().send(message);

      this.logger.info('Push notification sent successfully', {
        messageId,
        platform,
        title: payload.notification.title
      });

      return {
        success: true,
        messageId,
        platform
      };

    } catch (error: any) {
      this.logger.error('Failed to send push notification', {
        platform,
        error: error.message,
        errorCode: error.code
      });

      return {
        success: false,
        error: error.message || 'Unknown error occurred',
        platform
      };
    }
  }

  async sendToMultipleDevices(
    deviceTokens: string[],
    payload: PushNotificationPayload
  ): Promise<SendNotificationResult[]> {

    if (deviceTokens.length === 0) {
      this.logger.warn('No device tokens provided for multicast');
      return [];
    }

    try {
      this.initializeFirebase();

      if (deviceTokens.length === 1) {
        // Use single send for one device
        const result = await this.sendToDevice(deviceTokens[0], payload);
        return [result];
      }

      // Use multicast for multiple devices
      const message = {
        tokens: deviceTokens,
        notification: payload.notification,
        data: payload.data || {},
        android: payload.android,
        apns: payload.apns
      };

      this.logger.debug('Sending multicast push notification', {
        deviceCount: deviceTokens.length,
        title: payload.notification.title
      });

      const response = await admin.messaging().sendEachForMulticast(message);

      this.logger.info('Multicast push notification completed', {
        totalCount: deviceTokens.length,
        successCount: response.successCount,
        failureCount: response.failureCount
      });

      // Map results back to individual device tokens
      const results: SendNotificationResult[] = [];
      for (let i = 0; i < response.responses.length; i++) {
        const deviceResult = response.responses[i];

        if (deviceResult.success) {
          results.push({
            success: true,
            messageId: deviceResult.messageId
          });
        } else {
          results.push({
            success: false,
            error: deviceResult.error?.message || 'Unknown error'
          });

          // Log specific errors for debugging
          if (deviceResult.error) {
            this.logger.warn('Push notification failed for device', {
              deviceIndex: i,
              errorCode: deviceResult.error.code,
              errorMessage: deviceResult.error.message
            });
          }
        }
      }

      return results;

    } catch (error: any) {
      this.logger.error('Failed to send multicast push notification:', error);

      // Return error result for all devices
      return deviceTokens.map(() => ({
        success: false,
        error: error.message || 'Multicast send failed'
      }));
    }
  }

  async sendToTopic(
    topic: string,
    payload: PushNotificationPayload
  ): Promise<SendNotificationResult> {

    try {
      this.initializeFirebase();

      const message = {
        topic,
        notification: payload.notification,
        data: payload.data || {},
        android: payload.android,
        apns: payload.apns
      };

      this.logger.debug('Sending push notification to topic', {
        topic,
        title: payload.notification.title
      });

      const messageId = await admin.messaging().send(message);

      this.logger.info('Topic push notification sent successfully', {
        messageId,
        topic,
        title: payload.notification.title
      });

      return {
        success: true,
        messageId
      };

    } catch (error: any) {
      this.logger.error('Failed to send topic push notification', {
        topic,
        error: error.message
      });

      return {
        success: false,
        error: error.message || 'Topic send failed'
      };
    }
  }

  async validateToken(deviceToken: string): Promise<boolean> {
    try {
      this.initializeFirebase();

      // Try to send a dry run message to validate the token
      const message = {
        token: deviceToken,
        notification: {
          title: 'Test',
          body: 'Test'
        }
      };

      await admin.messaging().send(message, true); // dry run = true
      return true;

    } catch (error: any) {
      this.logger.debug('Device token validation failed', {
        tokenPrefix: deviceToken.substring(0, 20) + '...',
        errorCode: error.code
      });

      return false;
    }
  }

  /**
   * Check if an error indicates an invalid or unregistered device token
   */
  isInvalidTokenError(error: any): boolean {
    if (!error || !error.code) {
      return false;
    }

    const invalidTokenCodes = [
      'messaging/registration-token-not-registered',
      'messaging/invalid-registration-token',
      'messaging/invalid-argument'
    ];

    return invalidTokenCodes.includes(error.code);
  }

  /**
   * Get platform-specific notification configuration
   */
  createPlatformPayload(
    title: string,
    body: string,
    data: { [key: string]: string } = {},
    platform?: string
  ): PushNotificationPayload {

    const basePayload: PushNotificationPayload = {
      notification: {
        title,
        body,
        icon: 'app_icon',
        sound: 'default',
        click_action: 'FLUTTER_NOTIFICATION_CLICK'
      },
      data
    };

    // Add platform-specific configurations
    if (platform === 'android') {
      basePayload.android = {
        priority: 'high',
        notification: {
          channel_id: 'deals_channel',
          color: '#FF6B35',
          sound: 'default'
        }
      };
    } else if (platform === 'ios') {
      basePayload.apns = {
        headers: {
          'apns-priority': '10'
        },
        payload: {
          aps: {
            badge: 1,
            sound: 'default'
          }
        }
      };
    }

    return basePayload;
  }
}

// Export singleton instance
export const firebaseService = new FirebaseService();