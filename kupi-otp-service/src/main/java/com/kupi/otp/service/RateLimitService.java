package com.kupi.otp.service;

import com.kupi.otp.exception.OTPServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Service to handle OTP generation rate limiting
 */
@Service
@Slf4j
public class RateLimitService {

    private static final String RATE_LIMIT_KEY_PREFIX_EMAIL = "otp:rate-limit:email:";
    private static final String RATE_LIMIT_KEY_PREFIX_MOBILE = "otp:rate-limit:mobile:";

    private final StringRedisTemplate redisTemplate;
    private final int maxAttempts;
    private final long blockDurationInSeconds;
    private final ValueOperations<String, String> valueOps;

    @Autowired
    public RateLimitService(StringRedisTemplate redisTemplate,
                           @Value("${otp.rateLimit.maxAttempts}") int maxAttempts,
                           @Value("${otp.rateLimit.blockDurationInSeconds}") long blockDurationInSeconds) {
        this.redisTemplate = redisTemplate;
        this.maxAttempts = maxAttempts;
        this.blockDurationInSeconds = blockDurationInSeconds;
        this.valueOps = redisTemplate.opsForValue();
    }

    /**
     * Check if email OTP generation is allowed and increment attempt count
     *
     * @param email email address
     * @return true if allowed, false if rate limit exceeded
     */
    public boolean isEmailOtpGenerationAllowed(String email) {
        String key = RATE_LIMIT_KEY_PREFIX_EMAIL + email.toLowerCase().trim();
        return checkAndIncrementAttempts(key);
    }

    /**
     * Check if mobile OTP generation is allowed and increment attempt count
     *
     * @param mobileNo mobile number
     * @return true if allowed, false if rate limit exceeded
     */
    public boolean isMobileOtpGenerationAllowed(String mobileNo) {
        String key = RATE_LIMIT_KEY_PREFIX_MOBILE + mobileNo.trim();
        return checkAndIncrementAttempts(key);
    }

    /**
     * Check current attempt count and increment if allowed
     *
     * @param key Redis key for rate limiting
     * @return true if allowed, false if rate limit exceeded
     */
    private boolean checkAndIncrementAttempts(String key) {
        try {
            // First, check current value to see if we're already blocked
            String currentAttemptsStr = valueOps.get(key);
            log.debug("Rate limit check for key: {}. Current value from Redis: {}", key, currentAttemptsStr);
            
            if (currentAttemptsStr != null && !currentAttemptsStr.trim().isEmpty()) {
                int currentAttempts = Integer.parseInt(currentAttemptsStr.trim());
                log.debug("Parsed current attempts: {} for key: {}", currentAttempts, key);
                
                // If already at or above max attempts, block immediately
                if (currentAttempts >= maxAttempts) {
                    Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                    log.warn("Rate limit exceeded for key: {}. Current attempts: {}, Max attempts: {}, TTL: {} seconds",
                            key, currentAttempts, maxAttempts, ttl);
                    return false;
                }
            }

            // Use INCR for atomic increment (creates key with value 1 if it doesn't exist)
            Long newValue = redisTemplate.opsForValue().increment(key);
            log.debug("After INCR, new value: {} for key: {}", newValue, key);
            
            // Set TTL on first creation (when newValue is 1) or ensure TTL is set
            if (newValue == 1) {
                // Key was just created, set TTL
                redisTemplate.expire(key, blockDurationInSeconds, TimeUnit.SECONDS);
                log.debug("Set TTL to {} seconds for new key: {}", blockDurationInSeconds, key);
            } else {
                // Ensure TTL is set (in case it was missing)
                Long existingTtl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                if (existingTtl == null || existingTtl == -1) {
                    redisTemplate.expire(key, blockDurationInSeconds, TimeUnit.SECONDS);
                    log.debug("Reset TTL to {} seconds for key: {}", blockDurationInSeconds, key);
                }
            }
            
            // Double-check after increment (shouldn't happen if first check worked, but handles race conditions)
            if (newValue > maxAttempts) {
                log.warn("Rate limit exceeded after increment for key: {}. Current attempts: {}, Max attempts: {}",
                        key, newValue, maxAttempts);
                return false;
            }
            
            log.info("Rate limit check passed for key: {}. Current attempts: {}/{}", key, newValue, maxAttempts);
            return true;

        } catch (NumberFormatException e) {
            log.error("Invalid value format in Redis for key: {}. Value: {}. Resetting key.", 
                    key, valueOps.get(key), e);
            // Reset the key if value is corrupted
            redisTemplate.delete(key);
            // Try again with a fresh start
            Long newValue = redisTemplate.opsForValue().increment(key);
            redisTemplate.expire(key, blockDurationInSeconds, TimeUnit.SECONDS);
            log.info("Reset key and starting fresh. New value: {} for key: {}", newValue, key);
            return newValue <= maxAttempts;
        } catch (RuntimeException e) {
            log.error("Error while checking rate limit for key: {}", key, e);
            throw new OTPServiceException("Error while checking rate limit", e);
        }
    }

    /**
     * Get remaining attempts for email
     *
     * @param email email address
     * @return remaining attempts
     */
    public int getRemainingEmailAttempts(String email) {
        String key = RATE_LIMIT_KEY_PREFIX_EMAIL + email.toLowerCase().trim();
        return getRemainingAttempts(key);
    }

    /**
     * Get remaining attempts for mobile
     *
     * @param mobileNo mobile number
     * @return remaining attempts
     */
    public int getRemainingMobileAttempts(String mobileNo) {
        String key = RATE_LIMIT_KEY_PREFIX_MOBILE + mobileNo.trim();
        return getRemainingAttempts(key);
    }

    /**
     * Get remaining attempts for a key
     *
     * @param key Redis key
     * @return remaining attempts
     */
    private int getRemainingAttempts(String key) {
        try {
            String currentAttemptsStr = valueOps.get(key);
            if (currentAttemptsStr == null) {
                return maxAttempts;
            }
            int currentAttempts = Integer.parseInt(currentAttemptsStr);
            return Math.max(0, maxAttempts - currentAttempts);
        } catch (RuntimeException e) {
            log.error("Error while getting remaining attempts for key: {}", key, e);
            return maxAttempts;
        }
    }
}

