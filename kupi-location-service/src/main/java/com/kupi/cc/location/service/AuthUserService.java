package com.kupi.cc.location.service;


import com.kupi.cc.location.domain.entity.AuthUser;
import com.kupi.cc.location.domain.request.BulkBusinessProfileRequestDto;
import com.kupi.cc.location.domain.response.BulkBusinessResponseDto;
import com.kupi.cc.location.enums.UserType;
import com.kupi.cc.location.exception.InvalidUserException;
import com.kupi.cc.location.exception.KupiLocationException;
import com.kupi.cc.location.wrapper.AuthResponseWrapper;
import com.kupi.cc.location.wrapper.BulkBusinessProfileResponseWrapper;
import com.kupi.cc.location.wrapper.PreferredMerchantResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Auth service
 */
@Slf4j
@Service
public class AuthUserService {
    protected static final String TIME_ZONE_HEADER = "Time-Zone";
    protected static final String APP_KEY = "app-key";
    protected static final String TIME_ZONE_VALUE = "Asia/Colombo";
    private static final String USER_ID_HEADER = "User-Id";
    private static final String BULK_INFO_REPLACE_PHASE = "##TO-USER-TYPE##";
    private static final String FAILED_LOG_MESSAGE = "{}. statusCode: {}, body: {}";
    private static final String FAILED_TO_GET_BULK_BUSINESS_PROFILES =
            "Getting bulk business profile was failed from auth service";
    private static final String INVALID_USER_ID = "Invalid userId: ";
    private static final String FAILED_GET_USER_DATA = "Requesting user data from auth service was failed.";
    private final RestTemplate restTemplate;
    private final String getUserUrl;
    private final String getBulkMerchantsInfo;
    private final String getAppKey;
    private final String preferredMerchantUrl;

    @Autowired
    public AuthUserService(@Value("${auth.uri.getUser}") String getUserUrl,
                           @Value("${auth.baseUrl}") String baseUrl,
                           @Value("${auth.uri.getBulkMerchantInfo}") String getBulkMerchantInfo,
                           @Value("${auth.uri.getPreferredMerchant}") String getPreferredMerchantUrl,
                           @Value("${auth.appKey}") String getAppKey,
                           RestTemplate restTemplate) {
        this.getUserUrl = baseUrl + getUserUrl;
        this.getBulkMerchantsInfo = baseUrl + getBulkMerchantInfo;
        this.preferredMerchantUrl = baseUrl + getPreferredMerchantUrl;
        this.restTemplate = restTemplate;
        this.getAppKey = getAppKey;
    }

    /**
     * This method returns business information from auth service.
     *
     * @param userId  userId
     * @param userIds merchant/bank userIds
     * @return bulk business information.
     */
    public BulkBusinessResponseDto getBulkBusinessResponse(String userId, BulkBusinessProfileRequestDto userIds,
                                                           UserType toUserType) {
        HttpHeaders headers = getAuthHeadersForBulkMerchant();
        headers.set(USER_ID_HEADER, userId);
        HttpEntity<BulkBusinessProfileRequestDto> entity = new HttpEntity<>(userIds, headers);
        String getBulkInfoUrl = (UserType.MERCHANT.equals(toUserType)) ? getBulkMerchantsInfo
                .replace(BULK_INFO_REPLACE_PHASE, UserType.MERCHANT.toString()) :
                getBulkMerchantsInfo.replace(BULK_INFO_REPLACE_PHASE, UserType.BANK.toString());
        try {
            log.debug("Calling auth service to get bulk business profiles. url: {}, userId: {}",
                    getBulkInfoUrl, userId);
            ResponseEntity<BulkBusinessProfileResponseWrapper> result = restTemplate
                    .exchange(getBulkInfoUrl, HttpMethod.POST, entity, BulkBusinessProfileResponseWrapper.class);
            String responseBody = result.getBody() == null ? "" : Objects.requireNonNull(result.getBody()).toLogJson();
            log.debug("Getting bulk business profile was successful. statusCode: {}, response: {}",
                    result.getStatusCode(), responseBody);
            return Objects.requireNonNull(result.getBody()).getData();
        } catch (HttpClientErrorException e) {
            log.error(FAILED_LOG_MESSAGE, FAILED_TO_GET_BULK_BUSINESS_PROFILES, e.getRawStatusCode(),
                    e.getResponseBodyAsString());
            throw new KupiLocationException(FAILED_TO_GET_BULK_BUSINESS_PROFILES, e);
        }
    }

    /**
     * This method gets user By userId.
     *
     * @param userId userId
     * @param token  token
     * @return AuthUser
     */
    public AuthUser getUserByUserId(String userId, String token) {
        HttpHeaders headers = getAuthHeaders(token, userId);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        try {
            log.debug("Calling auth service to get the user by userId: {}, url: {}.", userId, getUserUrl);
            ResponseEntity<AuthResponseWrapper> result =
                    restTemplate.exchange(getUserUrl, HttpMethod.GET, entity, AuthResponseWrapper.class);
            String responseBody = Objects.requireNonNull(result.getBody()).getData().toLogJson();
            log.debug("Getting user by id was successful. statusCode: {}, response: {}",
                    result.getStatusCode(), responseBody);
            return Objects.requireNonNull(result.getBody()).getData();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == HttpStatus.BAD_REQUEST.value()) {
                throw new InvalidUserException(INVALID_USER_ID + userId);
            }
            throw new KupiLocationException(FAILED_GET_USER_DATA, e);
        }
    }

    /**
     * This method gets authHeaders url
     *
     * @param token  token
     * @param userId userId
     * @return headers
     */
    private HttpHeaders getAuthHeaders(String token, String userId) {
        var headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token.trim());
        headers.set(TIME_ZONE_HEADER, TIME_ZONE_VALUE);
        headers.set(USER_ID_HEADER, userId);
        return headers;
    }

    /**
     * This method returns header for getBulkBusinessResponse url.
     *
     * @return headers
     */
    private HttpHeaders getAuthHeadersForBulkMerchant() {
        var headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(TIME_ZONE_HEADER, TIME_ZONE_VALUE);
        headers.set(APP_KEY, getAppKey);
        return headers;
    }

    private HttpHeaders getPreferredMerchantHeaders(HttpHeaders httpHeaders, String userId) {
        httpHeaders.set(USER_ID_HEADER, userId);
        return httpHeaders;
    }

    /**
     * Calls Auth Service to fetch the preferred merchant list for a given user.
     *
     * @param userId user ID
     * @return list of preferred merchant IDs (empty list if none found)
     */
    public List<String> getPreferredMerchantIds(String userId) {


        HttpHeaders authHeadersForBulkMerchant = getPreferredMerchantHeaders(getAuthHeadersForBulkMerchant(), userId);
        HttpEntity<Void> entity = new HttpEntity<>(authHeadersForBulkMerchant);

        try {
            log.debug("Calling Auth Service to fetch preferred merchants for userId: {} at {}", userId, preferredMerchantUrl);
            ResponseEntity<PreferredMerchantResponseWrapper> response =
                    restTemplate.exchange(preferredMerchantUrl, HttpMethod.GET, entity, PreferredMerchantResponseWrapper.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                var data = response.getBody().getData();
                if (data != null && data.getPreferredMerchantIds() != null) {
                    log.debug("Preferred merchants for {}: {}", userId, data.getPreferredMerchantIds());
                    return data.getPreferredMerchantIds();
                }
            }
            log.warn("Auth service returned no preferred merchants for userId: {}", userId);
            return Collections.emptyList();
        } catch (HttpClientErrorException e) {
            log.error("Failed to fetch preferred merchants for userId: {}. Status: {}, Body: {}",
                    userId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new KupiLocationException("Failed to get preferred merchants from Auth Service", e);
        } catch (Exception e) {
            log.error("Unexpected error while fetching preferred merchants for userId: {}", userId, e);
            throw new KupiLocationException("Error calling Auth Service for preferred merchants", e);
        }
    }

    /**
     * Gets a hash of the user's preferred merchants for cache comparison.
     * This method creates a consistent hash that changes when preferred merchants change.
     *
     * @param userId user ID
     * @return hash string representing the current preferred merchants
     */
    public String getPreferredMerchantHash(String userId) {
        try {
            List<String> preferredMerchants = getPreferredMerchantIds(userId);
            
            // Sort the list to ensure consistent hash regardless of order
            Collections.sort(preferredMerchants);
            
            // Create hash from the sorted list
            String hashInput = String.join(",", preferredMerchants);
            int hash = hashInput.hashCode();
            
            log.debug("Generated preference hash for user {}: {} (merchants: {})", 
                    userId, hash, preferredMerchants);
            
            return String.valueOf(hash);
        } catch (Exception e) {
            log.error("Failed to generate preference hash for user {}: {}", userId, e.getMessage());
            // Return a default hash if there's an error
            return "error";
        }
    }
}