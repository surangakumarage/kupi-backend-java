package com.kupi.cc.location.service;

import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import com.kupi.cc.location.exception.CoreServiceHttpClientErrorException;
import com.kupi.cc.location.exception.GeoLocationHttpClientErrorException;
import com.kupi.cc.location.exception.NotificationServiceClientErrorException;
import com.kupi.cc.location.repository.CacheRepository;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PubSubService {

    private static final String SEPARATOR = "_";
    private static final String DEVICE_ID = "deviceId";
    private static final String USER_ID = "userId";
    private static final String LONGITUDE = "longitude";
    private static final String LATITUDE = "latitude";
    private static final String NOTIFIED_PREFIX = "notified_";
    private final CoreService coreService;
    private final CacheRepository cacheRepository;
    private final AuthUserService authUserService;
    private final String projectName;
    private final String topic;
    private final double movementThresholdKm;

    public PubSubService(@Value("${google.pubsub.project}") String projectName,
                         @Value("${google.pubsub.topic}") String topic,
                         @Value("${location.movement-threshold-km}") double movementThresholdKm,
                         CoreService coreService,
                         CacheRepository cacheRepository,
                         AuthUserService authUserService) {
        this.cacheRepository = cacheRepository;
        this.authUserService = authUserService;
        this.projectName = projectName;
        this.topic = topic;
        this.coreService = coreService;
        this.movementThresholdKm = movementThresholdKm;
    }

    /**
     * Subscribe to Google Pub/Sub and process incoming location updates.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void pubSubSubscribe() {
        try {
            ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(this.projectName, this.topic);
            MessageReceiver receiver = this::handleMessage;
            Subscriber subscriber = Subscriber.newBuilder(subscriptionName, receiver).build();
            subscriber.startAsync().awaitRunning();
        } catch (CoreServiceHttpClientErrorException e) {
            log.error("Failed to get deals from CoreService.", e);
        } catch (NotificationServiceClientErrorException e) {
            log.error("Failed to send notification due to notification service.", e);
        } catch (GeoLocationHttpClientErrorException e) {
            log.error("Failed to get geoCode from google map service.", e);
        } catch (Exception e) {
            log.error("Failed to start Pub/Sub subscriber.", e);
        }
    }

    /**
     * Handles a single message received from Pub/Sub.
     */
    private void handleMessage(PubsubMessage message, AckReplyConsumer consumer) {
        String data = message.getData().toStringUtf8();
        log.info("------------------------------------------------------------------------------------------------------");

        log.info("Received new Pub/Sub message.");
        log.info("PubSub message JSON: {}", data);

        // Log local time from the message
        try {
            JSONObject json = new JSONObject(data);
            String timestampStr = json.getString("timestamp");

            // Normalize and format timestamp
            String normalizedTimestamp = timestampStr.substring(0, timestampStr.indexOf('.') + 4) + "Z";
            Instant instant = Instant.parse(normalizedTimestamp);
            ZonedDateTime readableTime = instant.atZone(ZoneId.systemDefault());
            String humanReadableTime = readableTime.format(DateTimeFormatter.ofPattern("MMMM d, yyyy, h:mm:ss a"));

            log.info("Message time: {} (local system time zone)", humanReadableTime);
        } catch (Exception e) {
            log.warn("Failed to format message timestamp: {}", e.getMessage());
        }

        try {
            JSONObject json = new JSONObject(data);

            String userId = json.getString(USER_ID);
            String deviceId = json.getString(DEVICE_ID);
            double newLat = json.getDouble(LATITUDE);
            double newLon = json.getDouble(LONGITUDE);
            log.info("Starting Process Location  for userId: {}, deviceId: {}, lat: {}, lon: {}",
                    userId, deviceId, newLat, newLon);
            log.info("--------------------------------------------------------------------------------------------------");

            // Parse the original message time for logging
            String originalMessageTime = null;
            try {
                String timestampStr = json.getString("timestamp");
                String timeZoneStr = json.getString("timeZone");
                String normalizedTimestamp = timestampStr.substring(0, timestampStr.indexOf('.') + 4) + "Z";
                Instant instant = Instant.parse(normalizedTimestamp);
                ZonedDateTime localTime = instant.atZone(ZoneId.of(timeZoneStr));
                originalMessageTime = localTime.format(DateTimeFormatter.ofPattern("MMMM d, yyyy, h:mm:ss a"));
            } catch (Exception e) {
                log.warn("Failed to parse original message time: {}", e.getMessage());
            }

            processLocationUpdate(userId, deviceId, newLat, newLon, originalMessageTime);
            log.info("Processed location update for user {} at ({}, {}) - Device ID: {}", userId, newLat, newLon, deviceId);
            consumer.ack(); // Only ack on success
        } catch (Exception e) {
            log.error("Handling pubsub message was failed: {}", data, e);
            consumer.nack(); // Don't ack on failure - let PubSub retry
        }
    }

    /**
     * Checks if a merchant can be notified for this user and device.
     * Returns true if the merchant has NOT been notified recently (can notify).
     * Returns false if the merchant has been notified recently (cannot notify).
     */
    private boolean canNotifyMerchant(String userId, String deviceId, String merchantId) {
        String notifiedKey = NOTIFIED_PREFIX + userId + SEPARATOR + deviceId + SEPARATOR + merchantId;
        return !cacheRepository.get(notifiedKey).isPresent();
    }

    /**
     * Marks a merchant as notified for this user and device with TTL.
     * This prevents duplicate notifications for the same merchant on the same device.
     */
    private void markMerchantAsNotified(String userId, String deviceId, String merchantId) {
        String notifiedKey = NOTIFIED_PREFIX + userId + SEPARATOR + deviceId + SEPARATOR + merchantId;
        cacheRepository.put(notifiedKey, "notified");
        log.info("Marked merchant {} as notified for user {} on device {} with TTL", merchantId, userId, deviceId);
    }

    /**
     * Processes the location update: compares with cache and triggers CoreService if needed.
     * Uses hash-based preference change detection to avoid spam notifications.
     */
    private void processLocationUpdate(String userId, String deviceId, double newLat, double newLon, String originalMessageTime) {
        String cacheKey = deviceId + SEPARATOR + userId;
        var cachedValue = cacheRepository.get(cacheKey);
        
        log.info("Checking cache for key {} ... found? {}", cacheKey, cachedValue.isPresent());

        // Get current user preferences
        UserPreferences currentPreferences = getUserPreferences(userId);
        String newCacheValue = buildCacheValue(newLat, newLon, currentPreferences);

        if (cachedValue.isPresent()) {
            handleExistingLocationUpdate(userId, deviceId, newLat, newLon, originalMessageTime, 
                    cachedValue.get(), newCacheValue, cacheKey, currentPreferences);
        } else {
            handleFirstLocationUpdate(userId, deviceId, newLat, newLon, originalMessageTime, 
                    newCacheValue, cacheKey, currentPreferences);
        }
    }

    /**
     * Represents user preferences for caching and comparison.
     */
    private static class UserPreferences {
        final String preferenceHash;
        final List<String> preferredMerchants;
        
        UserPreferences(String preferenceHash, List<String> preferredMerchants) {
            this.preferenceHash = preferenceHash;
            this.preferredMerchants = preferredMerchants;
        }
    }

    /**
     * Gets current user preferences and logs debug information.
     */
    private UserPreferences getUserPreferences(String userId) {
        String currentPreferenceHash = authUserService.getPreferredMerchantHash(userId);
        List<String> currentPreferredMerchants = authUserService.getPreferredMerchantIds(userId);

        // Debug logging
        logUserPreferences(userId, currentPreferredMerchants, currentPreferenceHash);
        
        return new UserPreferences(currentPreferenceHash, currentPreferredMerchants);
    }

    /**
     * Logs user preferences for debugging.
     */
    private void logUserPreferences(String userId, List<String> preferredMerchants, String preferenceHash) {
        log.info("Current preferred merchants for user {}: {}", userId, preferredMerchants);
        log.info("Current preferred merchants size: {}", preferredMerchants.size());
        log.info("Current preferred merchants type: {}", preferredMerchants.getClass().getSimpleName());
        for (int i = 0; i < preferredMerchants.size(); i++) {
            log.info("Merchant {}: '{}' (length: {})", i, preferredMerchants.get(i), preferredMerchants.get(i).length());
        }
        log.info("Current preference hash for user {}: {}", userId, preferenceHash);
    }

    /**
     * Builds cache value string from location and preferences.
     */
    private String buildCacheValue(double newLat, double newLon, UserPreferences preferences) {
        String joinedMerchants = String.join(",", preferences.preferredMerchants);
        log.info("String.join(',', merchants) result: '{}'", joinedMerchants);
        
        String newCacheValue = newLat + "," + newLon + "," + preferences.preferenceHash + "," + joinedMerchants;
        log.info("New cache value: {}", newCacheValue);
        
        return newCacheValue;
    }

    /**
     * Handles location update when previous location exists in cache.
     */
    private void handleExistingLocationUpdate(String userId, String deviceId, double newLat, double newLon, 
            String originalMessageTime, String cachedValue, String newCacheValue, String cacheKey, 
            UserPreferences currentPreferences) {
        
        log.info("Retrieved cache value for user {}: {}", userId, cachedValue);
        String[] parts = cachedValue.split(",");
        log.info("Split cache parts for user {}: {}", userId, Arrays.toString(parts));

        if (parts.length >= 4) {
            LocationCacheData oldData = parseLocationCacheData(parts);
            log.info("Old preferred merchants for user {}: {}", userId, oldData.preferredMerchants);

            boolean preferencesChanged = !oldData.preferenceHash.equals(currentPreferences.preferenceHash);
            double distanceKm = calculateDistanceKm(oldData.latitude, oldData.longitude, newLat, newLon);

            if (distanceKm >= movementThresholdKm || preferencesChanged) {
                String reason = preferencesChanged ? "preferences changed" :
                        String.format("movement %.2f km exceeds threshold (%.2f km)", distanceKm, movementThresholdKm);
                
                log.info("Triggering update for user {}: {}", userId, reason);
                cacheRepository.put(cacheKey, newCacheValue);

                if (preferencesChanged) {
                    handlePreferenceChangeNotification(userId, deviceId, newLat, newLon, originalMessageTime, 
                            oldData.preferredMerchants, currentPreferences.preferredMerchants);
                } else {
                    handleMovementNotification(userId, deviceId, newLat, newLon, originalMessageTime, 
                            currentPreferences.preferredMerchants, distanceKm);
                }
            } else {
                log.info("No action for user {}: movement {:.2f} km below threshold ({} km) and preferences unchanged.",
                        userId, distanceKm, movementThresholdKm);
            }
        }
    }

    /**
     * Represents cached location data.
     */
    private static class LocationCacheData {
        final double latitude;
        final double longitude;
        final String preferenceHash;
        final String preferredMerchants;
        
        LocationCacheData(double latitude, double longitude, String preferenceHash, String preferredMerchants) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.preferenceHash = preferenceHash;
            this.preferredMerchants = preferredMerchants;
        }
    }

    /**
     * Parses cached location data from string array.
     */
    private LocationCacheData parseLocationCacheData(String[] parts) {
        double oldLat = Double.parseDouble(parts[0]);
        double oldLon = Double.parseDouble(parts[1]);
        String oldPreferenceHash = parts[2];
        String oldPreferredMerchants = String.join(",", Arrays.copyOfRange(parts, 3, parts.length));
        
        return new LocationCacheData(oldLat, oldLon, oldPreferenceHash, oldPreferredMerchants);
    }

    /**
     * Handles notifications when user preferences change.
     */
    private void handlePreferenceChangeNotification(String userId, String deviceId, double newLat, double newLon, 
            String originalMessageTime, String oldPreferredMerchants, List<String> currentPreferredMerchants) {
        
        List<String> oldMerchants = Arrays.asList(oldPreferredMerchants.split(","));
        List<String> newlyAddedMerchants = currentPreferredMerchants.stream()
                .filter(merchant -> !oldMerchants.contains(merchant))
                .collect(Collectors.toList());

        List<String> removedMerchants = oldMerchants.stream()
                .filter(merchant -> !currentPreferredMerchants.contains(merchant))
                .collect(Collectors.toList());

        log.info("Merchant changes for user {} - Added: {}, Removed: {}", userId, newlyAddedMerchants, removedMerchants);

        if (!newlyAddedMerchants.isEmpty()) {
            processNewlyAddedMerchants(userId, deviceId, newLat, newLon, originalMessageTime, newlyAddedMerchants);
        } else if (!removedMerchants.isEmpty()) {
            log.info("Merchants removed for user {}: {}. No notifications will be sent for removed merchants.", userId, removedMerchants);
        } else {
            log.info("No merchant changes detected for user {}. Skipping notification.", userId);
        }
    }

    /**
     * Processes notifications for newly added merchants.
     */
    private void processNewlyAddedMerchants(String userId, String deviceId, double newLat, double newLon, 
            String originalMessageTime, List<String> newlyAddedMerchants) {
        
        log.info("Newly added preferred merchants for user {}: {}", userId, newlyAddedMerchants);
        
        List<String> merchantsToNotify = newlyAddedMerchants.stream()
                .filter(merchantId -> canNotifyMerchant(userId, deviceId, merchantId))
                .collect(Collectors.toList());

        if (!merchantsToNotify.isEmpty()) {
            log.info("Notifying for newly added merchants (not recently notified): {}", merchantsToNotify);
            sendNotificationAndMarkAsNotified(userId, deviceId, newLat, newLon, originalMessageTime, merchantsToNotify);
        } else {
            log.info("All newly added merchants have been recently notified for user {}. Skipping notification.", userId);
        }
    }

    /**
     * Handles movement-based notifications.
     */
    private void handleMovementNotification(String userId, String deviceId, double newLat, double newLon, 
            String originalMessageTime, List<String> currentPreferredMerchants, double distanceKm) {
        
        log.info("Movement-based notification for user {} - distance: {:.2f} km", userId, distanceKm);

        List<String> merchantsToNotify = currentPreferredMerchants.stream()
                .filter(merchantId -> canNotifyMerchant(userId, deviceId, merchantId))
                .collect(Collectors.toList());

        if (!merchantsToNotify.isEmpty()) {
            log.info("Movement notification for merchants not recently notified: {}", merchantsToNotify);
            sendNotificationAndMarkAsNotified(userId, deviceId, newLat, newLon, originalMessageTime, merchantsToNotify);
        } else {
            log.info("All preferred merchants have been recently notified for user {}. Skipping movement notification.", userId);
        }
    }

    /**
     * Handles first location update for a user/device combination.
     */
    private void handleFirstLocationUpdate(String userId, String deviceId, double newLat, double newLon, 
            String originalMessageTime, String newCacheValue, String cacheKey, UserPreferences currentPreferences) {
        
        cacheRepository.put(cacheKey, newCacheValue);
        log.info("First location recorded for user {}. Triggering CoreService immediately.", userId);

        List<String> merchantsToNotify = currentPreferences.preferredMerchants.stream()
                .filter(merchantId -> canNotifyMerchant(userId, deviceId, merchantId))
                .collect(Collectors.toList());

        if (!merchantsToNotify.isEmpty()) {
            log.info("First location - notifying for merchants not recently notified: {}", merchantsToNotify);
            sendNotificationAndMarkAsNotified(userId, deviceId, newLat, newLon, originalMessageTime, merchantsToNotify);
        } else {
            log.info("All preferred merchants have been recently notified for user {}. Skipping first location notification.", userId);
        }
    }

    /**
     * Sends notification and marks merchants as notified.
     */
    private void sendNotificationAndMarkAsNotified(String userId, String deviceId, double newLat, double newLon, 
            String originalMessageTime, List<String> merchantsToNotify) {
        
        coreService.getMerchantIdListForSpecificMerchants(newLat, newLon, userId, merchantsToNotify, originalMessageTime);
        merchantsToNotify.forEach(merchantId -> markMerchantAsNotified(userId, deviceId, merchantId));
    }

    /**
     * Calculates distance between two coordinates in kilometers.
     */
    private double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
