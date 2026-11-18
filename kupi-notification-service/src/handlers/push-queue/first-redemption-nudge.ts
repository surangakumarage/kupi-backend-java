import { ScheduledHandler } from 'aws-lambda';
import { getClient } from '../../services/database/connection';
import { Logger } from '../../utils/logger';

interface User {
  id: number;
  first_name: string;
  last_name: string;
  email: string;
  mobile_number: string;
  created_at: Date;
  days_since_signup: number;
}

interface Deal {
  id: number;
  merchant_id: number;
  merchant_name: string;
  title: string;
  description: string;
  deal_link: string;
  discount_percentage: number;
  original_price: number;
  discounted_price: number;
  category: string;
  created_at: Date;
}

interface MerchantDeals {
  merchant_id: number;
  merchant_name: string;
  deals: Deal[];
}

export const handler: ScheduledHandler = async (event, context) => {
  const logger = new Logger('FirstRedemptionNudgeProcessor');
  let client;

  try {
    logger.info('Starting FIRST_REDEMPTION_NUDGE processing...', {
      time: new Date().toISOString(),
      event: event
    });

    client = await getClient();

    // 1. Find eligible users (no purchases, 3 days after signup, not exceeding 4 days)
    const eligibleUsersQuery = `
      SELECT
        u.id,
        u.first_name,
        u.last_name,
        u.email,
        u.mobile_number,
        u.created_at,
        EXTRACT(DAY FROM (CURRENT_TIMESTAMP - u.created_at)) as days_since_signup
      FROM users u
      LEFT JOIN purchases p ON u.id = p.user_id
      WHERE u.is_active = true
        AND u.is_deleted = false
        AND p.id IS NULL  -- No purchases
        AND EXTRACT(DAY FROM (CURRENT_TIMESTAMP - u.created_at)) >= 3
        AND EXTRACT(DAY FROM (CURRENT_TIMESTAMP - u.created_at)) <= 4
        AND u.created_at >= CURRENT_TIMESTAMP - INTERVAL '4 days'
    `;

    const usersResult = await client.query(eligibleUsersQuery);
    const eligibleUsers: User[] = usersResult.rows;

    logger.info(`Found ${eligibleUsers.length} eligible users for FIRST_REDEMPTION_NUDGE`);

    if (eligibleUsers.length === 0) {
      logger.info('No eligible users found, ending processing');
      return {
        statusCode: 200,
        body: JSON.stringify({
          message: 'No eligible users found',
          processedUsers: 0
        })
      };
    }

    // 2. Get recent deals (randomly selected, up to 3)
    const recentDealsQuery = `
      SELECT
        d.id,
        d.merchant_id,
        m.name as merchant_name,
        d.title,
        d.description,
        d.deal_link,
        d.discount_percentage,
        d.original_price,
        d.discounted_price,
        d.category,
        d.created_at
      FROM deals d
      INNER JOIN merchants m ON d.merchant_id = m.id
      WHERE d.is_active = true
        AND d.end_date > CURRENT_TIMESTAMP
        AND m.is_active = true
      ORDER BY RANDOM()
      LIMIT 3
    `;

    const dealsResult = await client.query(recentDealsQuery);
    const selectedDeals: Deal[] = dealsResult.rows;

    logger.info(`Selected ${selectedDeals.length} deals for notifications`);

    if (selectedDeals.length === 0) {
      logger.info('No active deals found, ending processing');
      return {
        statusCode: 200,
        body: JSON.stringify({
          message: 'No active deals found',
          processedUsers: 0
        })
      };
    }

    // 3. Group deals by merchant
    const merchantGroups = groupDealsByMerchant(selectedDeals);

    let processedUsers = 0;
    let skippedUsers = 0;

    // 4. Process each eligible user
    for (const user of eligibleUsers) {
      try {
        const result = await processUserNotifications(user, merchantGroups, client, logger);
        if (result.success) {
          processedUsers++;
        } else {
          skippedUsers++;
        }
      } catch (error) {
        logger.error(`Failed to process user ${user.id}:`, error);
        skippedUsers++;
      }
    }

    logger.info(`FIRST_REDEMPTION_NUDGE processing completed`, {
      processedUsers,
      skippedUsers,
      totalUsers: eligibleUsers.length,
      dealCount: selectedDeals.length,
      merchantGroups: merchantGroups.length
    });

    return {
      statusCode: 200,
      body: JSON.stringify({
        message: 'FIRST_REDEMPTION_NUDGE processing completed',
        processedUsers,
        skippedUsers,
        totalUsers: eligibleUsers.length,
        deals: selectedDeals.map(d => ({
          id: d.id,
          title: d.title,
          merchant_name: d.merchant_name
        }))
      })
    };

  } catch (error: any) {
    logger.error('Failed to process FIRST_REDEMPTION_NUDGE:', error);
    throw error;
  } finally {
    if (client) {
      client.release();
    }
  }
};

function groupDealsByMerchant(deals: Deal[]): MerchantDeals[] {
  const merchantMap = new Map<number, MerchantDeals>();

  for (const deal of deals) {
    if (!merchantMap.has(deal.merchant_id)) {
      merchantMap.set(deal.merchant_id, {
        merchant_id: deal.merchant_id,
        merchant_name: deal.merchant_name,
        deals: []
      });
    }
    merchantMap.get(deal.merchant_id)!.deals.push(deal);
  }

  return Array.from(merchantMap.values());
}

async function processUserNotifications(
  user: User,
  merchantGroups: MerchantDeals[],
  client: any,
  logger: Logger
): Promise<{ success: boolean; reason?: string }> {

  const templateCode = 'FIRST_REDEMPTION_NUDGE';

  // Validate total deal count equals exactly 3
  const totalDeals = merchantGroups.reduce((sum, group) => sum + group.deals.length, 0);
  if (totalDeals !== 3) {
    logger.error(`Invalid deal distribution: total deals = ${totalDeals}, expected = 3`, {
      userId: user.id,
      merchantGroups: merchantGroups.map(g => ({
        merchant: g.merchant_name,
        dealCount: g.deals.length
      }))
    });
    return { success: false, reason: 'invalid_deal_count' };
  }

  // Enhanced duplicate prevention - check for ANY pending FIRST_REDEMPTION_NUDGE for this user
  const duplicateCheckQuery = `
    SELECT COUNT(*) as count
    FROM push_message_queue
    WHERE user_id = $1
      AND template_code = $2
      AND status IN ('PENDING', 'PROCESSING')
      AND created_at > CURRENT_TIMESTAMP - INTERVAL '24 hours'
  `;

  const duplicateResult = await client.query(duplicateCheckQuery, [user.id, templateCode]);
  const existingCount = parseInt(duplicateResult.rows[0].count);

  if (existingCount > 0) {
    logger.info(`User ${user.id} already has ${existingCount} pending ${templateCode} messages, skipping`);
    return { success: false, reason: 'duplicate_prevention' };
  }

  // Create separate messages per merchant group
  let createdMessages = 0;
  const createdJobIds: string[] = [];

  for (const merchantGroup of merchantGroups) {
    try {
      const jobId = await createJobId(templateCode, user.id, merchantGroup.merchant_id);

      const messagePayload = createMessagePayload(user, merchantGroup);

      const insertQuery = `
        INSERT INTO push_message_queue (
          job_id,
          user_id,
          template_code,
          status,
          payload,
          scheduled_at
        ) VALUES ($1, $2, $3, $4, $5, $6)
        RETURNING id
      `;

      const insertValues = [
        jobId,
        user.id,
        templateCode,
        'PENDING',
        JSON.stringify(messagePayload),
        new Date() // Send immediately
      ];

      const insertResult = await client.query(insertQuery, insertValues);
      const queueId = insertResult.rows[0].id;

      logger.info(`Created push notification queue entry`, {
        queueId,
        jobId,
        userId: user.id,
        merchantName: merchantGroup.merchant_name,
        dealCount: merchantGroup.deals.length,
        totalDealsInBatch: totalDeals
      });

      createdMessages++;
      createdJobIds.push(jobId);

    } catch (error) {
      logger.error(`Failed to create queue entry for user ${user.id}, merchant ${merchantGroup.merchant_id}:`, error);
      // If any message fails, clean up previously created messages for this user
      if (createdJobIds.length > 0) {
        await cleanupFailedMessages(client, createdJobIds, logger);
      }
      return { success: false, reason: 'failed_to_create_messages' };
    }
  }

  logger.info(`Successfully created ${createdMessages} messages for user ${user.id}`, {
    totalDealsDistributed: totalDeals,
    merchantGroupCount: merchantGroups.length,
    jobIds: createdJobIds
  });

  return {
    success: createdMessages > 0,
    reason: createdMessages === 0 ? 'failed_to_create_messages' : undefined
  };
}

async function cleanupFailedMessages(client: any, jobIds: string[], logger: Logger): Promise<void> {
  try {
    const deleteQuery = `
      DELETE FROM push_message_queue
      WHERE job_id = ANY($1) AND status = 'PENDING'
    `;
    await client.query(deleteQuery, [jobIds]);
    logger.info(`Cleaned up ${jobIds.length} failed message entries`);
  } catch (error) {
    logger.error('Failed to cleanup failed messages:', error);
  }
}

async function createJobId(templateCode: string, userId: number, merchantId: number): Promise<string> {
  const timestamp = Date.now();
  const randomSuffix = Math.random().toString(36).substring(2, 8);
  return `${templateCode}_${userId}_${merchantId}_${timestamp}_${randomSuffix}`;
}

function createMessagePayload(user: User, merchantGroup: MerchantDeals): any {
  const userName = `${user.first_name} ${user.last_name}`.trim() || 'User';

  // Create message based on number of deals
  let title: string;
  let body: string;

  if (merchantGroup.deals.length === 1) {
    const deal = merchantGroup.deals[0];
    title = `Great deal from ${merchantGroup.merchant_name}! 🎉`;
    body = `Hi ${user.first_name}! ${deal.title} - Save ${deal.discount_percentage}% at ${merchantGroup.merchant_name}`;
  } else {
    title = `${merchantGroup.deals.length} amazing deals from ${merchantGroup.merchant_name}! 🔥`;
    body = `Hi ${user.first_name}! We found ${merchantGroup.deals.length} great deals at ${merchantGroup.merchant_name} just for you`;
  }

  return {
    notification: {
      title,
      body,
      icon: 'app_icon',
      sound: 'default',
      click_action: 'FLUTTER_NOTIFICATION_CLICK'
    },
    data: {
      type: 'first_redemption_nudge',
      template_code: 'FIRST_REDEMPTION_NUDGE',
      user_id: user.id.toString(),
      merchant_id: merchantGroup.merchant_id.toString(),
      merchant_name: merchantGroup.merchant_name,
      deal_count: merchantGroup.deals.length.toString(),
      deals: merchantGroup.deals.map(deal => ({
        id: deal.id.toString(),
        title: deal.title,
        description: deal.description,
        deal_link: deal.deal_link,
        discount_percentage: deal.discount_percentage.toString(),
        original_price: deal.original_price.toString(),
        discounted_price: deal.discounted_price.toString(),
        category: deal.category
      }))
    },
    android: {
      priority: 'high',
      notification: {
        channel_id: 'deals_channel',
        color: '#FF6B35',
        tag: `redemption_${merchantGroup.merchant_id}`
      }
    },
    apns: {
      headers: {
        'apns-priority': '10',
        'apns-collapse-id': `redemption_${merchantGroup.merchant_id}`
      },
      payload: {
        aps: {
          badge: 1,
          sound: 'default'
        }
      }
    }
  };
}