package com.kupi.cc.base.service;

import com.kupi.cc.base.exception.CouponGenerationException;
import com.kupi.cc.base.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service for generating unique coupon codes
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CouponCodeGeneratorService {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;
    private static final int MAX_RETRY_ATTEMPTS = 10;

    private final CouponRepository couponRepository;

    /**
     * Generate a single unique coupon code
     *
     * @return unique coupon code
     */
    public String generateUniqueCode() {
        for (int attempt = 0; attempt < MAX_RETRY_ATTEMPTS; attempt++) {
            String code = generateRandomCode();
            if (isCodeUnique(code)) {
                log.debug("Generated unique coupon code: {}", code);
                return code;
            }
        }
        log.error("Failed to generate unique code after {} attempts", MAX_RETRY_ATTEMPTS);
        throw new CouponGenerationException("Unable to generate unique code after " + MAX_RETRY_ATTEMPTS + " attempts");
    }

    /**
     * Generate multiple unique coupon codes
     *
     * @param count number of codes to generate
     * @return list of unique coupon codes
     */
    public List<String> generateUniqueCodes(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be positive");
        }

        List<String> codes = new ArrayList<>();
        Set<String> generatedCodes = new HashSet<>();
        int attempts = 0;
        int maxAttempts = count * 3; // Allow some retries for collisions

        log.info("Generating {} unique coupon codes", count);

        while (codes.size() < count && attempts < maxAttempts) {
            String code = generateRandomCode();

            if (!generatedCodes.contains(code) && isCodeUnique(code)) {
                codes.add(code);
                generatedCodes.add(code);
                log.debug("Generated code {}/{}: {}", codes.size(), count, code);
            }
            attempts++;
        }

        if (codes.size() < count) {
            log.error("Failed to generate {} codes. Generated: {}, Attempts: {}", count, codes.size(), attempts);
            throw new CouponGenerationException("Failed to generate " + count + " unique codes. Generated: " + codes.size());
        }

        log.info("Successfully generated {} unique coupon codes", codes.size());
        return codes;
    }

    /**
     * Generate a random 6-character code
     *
     * @return random coupon code
     */
    private String generateRandomCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }

        return code.toString();
    }

    /**
     * Check if a coupon code is unique in the database
     *
     * @param couponCode code to check
     * @return true if unique, false otherwise
     */
    private boolean isCodeUnique(String couponCode) {
        try {
            return !couponRepository.existsByCouponCode(couponCode);
        } catch (Exception e) {
            log.error("Error checking code uniqueness: {}", couponCode, e);
            return false;
        }
    }

}
