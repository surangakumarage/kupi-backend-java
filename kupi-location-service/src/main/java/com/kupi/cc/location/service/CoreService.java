package com.kupi.cc.location.service;


import com.kupi.cc.location.domain.entity.Branch;
import com.kupi.cc.location.domain.request.BulkBusinessProfileRequestDto;
import com.kupi.cc.location.domain.request.BulkUserRequestDto;
import com.kupi.cc.location.domain.request.PushNotificationRequestDto;
import com.kupi.cc.location.domain.response.*;
import com.kupi.cc.location.enums.ApprovalStatus;
import com.kupi.cc.location.enums.DealFilterType;
import com.kupi.cc.location.enums.UserType;
import com.kupi.cc.location.exception.CoreServiceHttpClientErrorException;
import com.kupi.cc.location.wrapper.BulkDealResponseWrapper;
import com.kupi.cc.location.wrapper.BulkMerchantCategoriesResponseWrapper;
import com.kupi.cc.location.wrapper.DealListResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Core Service
 */
@Slf4j
@Service
public class CoreService {

    private static final String TIME_ZONE_HEADER = "Time-Zone";
    private static final String APP_KEY = "App-key";
    private static final String USER_ID_HEADER = "User-Id";
    private static final String INTERNAL_USER_ID = "internal"; // Placeholder user ID for internal service calls
    private static final int FIRST_PAGE = 0;
    private static final String FAILED_TO_GET_BULK_DEALS = "Failed to get bulk deal by merchant ids from calling core service.";
    private static final String FAILED_TO_GET_BULK_CATEGORIES = "Failed to get bulk categories for merchants from calling core service.";
    private static final String FAILED_TO_GET_RECENTLY_EXPIRE_DEALS = "Failed to get recently expiring deals from calling core service.";
    private final String appKey;
    private final int merchantSize;
    private final RestTemplate restTemplate;
    private final String getMerchantInfoUrl;
    private final String getDealsByIdsUrl;
    private final String getBulkCategoriesUrl;
    private final String getRecentlyExpireDealsUrl;
    private final String getDealsOfTheDayUrl;
    private final BranchService branchService;
    private final NotificationService notificationService;
    private final AuthUserService authUserService;
    protected static final String TIME_ZONE_VALUE = "Asia/Colombo";
    private final double searchRadiusKm;
    private final double dealsSearchRadiusKm;


    public CoreService(@Value("${core.baseUrl}") String baseUrl,
                       @Value("${core.getBulkDealInfo}") String getBulkDealUrl,
                       @Value("${core.getDealsByIds}") String getDealsByIdsPath,
                       @Value("${core.getBulkCategories}") String getBulkCategoriesUrl,
                       @Value("${core.getRecentlyExpireDeals}") String getRecentlyExpireDealsUrl,
                       @Value("${core.getDealsOfTheDay}") String getDealsOfTheDayUrl,
                       @Value("${core.getMerchantSize}") String getMerchantSize,
                       @Value("${auth.appKey}") String getAppKey,
                       RestTemplate restTemplate, BranchService branchService, NotificationService notificationService,
                       AuthUserService authUserService,
                       @Value("${location.search-radius-km}") double searchRadiusKm,
                       @Value("${location.deals-search-radius-km}") double dealsSearchRadiusKm) {

        this.branchService = branchService;
        this.notificationService = notificationService;
        this.authUserService = authUserService;
        this.searchRadiusKm = searchRadiusKm;
        this.dealsSearchRadiusKm = dealsSearchRadiusKm;
        this.getMerchantInfoUrl = baseUrl + getBulkDealUrl;
        this.getDealsByIdsUrl = baseUrl + getDealsByIdsPath;
        this.getBulkCategoriesUrl = baseUrl + getBulkCategoriesUrl;
        this.getRecentlyExpireDealsUrl = baseUrl + getRecentlyExpireDealsUrl;
        this.getDealsOfTheDayUrl = baseUrl + getDealsOfTheDayUrl;
        this.restTemplate = restTemplate;
        this.appKey = getAppKey;
        this.merchantSize = Integer.parseInt(getMerchantSize);
    }


    public void getMerchantIdList(double latitude, double longitude, String userId) {

        // Fetch preferred merchants for this user
        List<String> preferredMerchants = authUserService.getPreferredMerchantIds(userId);
        log.info("Fetched {} preferred merchants for user {}.", preferredMerchants.size(), userId);

        if (preferredMerchants.isEmpty()) {
            log.info("User {} has no preferred merchants. Skipping deal search.", userId);
            return;
        }

        int currentPage = FIRST_PAGE;
        int totalPage;

        boolean hasMorePages = true;
        while (hasMorePages) {
            Pageable pageable = PageRequest.of(currentPage, merchantSize);

            Page<String> merchantIdListByLocation = branchService.getPreferredMerchantIdListByLocation(
                    pageable, latitude, longitude, searchRadiusKm, preferredMerchants
            );

            log.debug("Page {} → found {} nearby merchants (within {} km).",
                    currentPage,
                    merchantIdListByLocation.getTotalElements(),
                    searchRadiusKm);

            if (!merchantIdListByLocation.hasContent()) {
                log.info("No nearby merchants found for user {} at location (lat: {}, lon: {}).",
                        userId, latitude, longitude);
                break;
            }

            totalPage = merchantIdListByLocation.getTotalPages();
            List<String> merchantIdList = merchantIdListByLocation.getContent();

            log.info("Found {} nearby preferred merchants for user {} on page {}.",
                    merchantIdList.size(), userId, currentPage);

            // Fetch bulk deals for these merchants
            BulkMerchantDealsResponseDto bulkDealsByMerchantIds = getBulkDealsByMerchantIds(merchantIdList);

            boolean hasDeals = bulkDealsByMerchantIds != null
                    && bulkDealsByMerchantIds.getDealGroups() != null
                    && !bulkDealsByMerchantIds.getDealGroups().isEmpty();

            if (hasDeals) {
                log.info("Received {} deal groups for user {} (page: {}, lat: {}, lon: {}).",
                        bulkDealsByMerchantIds.getDealGroups().size(),
                        userId, currentPage, latitude, longitude);

                PushNotificationRequestDto pushNotificationRequestDto =
                        new PushNotificationRequestDto(userId, bulkDealsByMerchantIds);
                notificationService.sendNotification(pushNotificationRequestDto);

                // Get current Sri Lankan time
                ZonedDateTime sriLankanTime = ZonedDateTime.now(ZoneId.of(TIME_ZONE_VALUE));
                String humanReadableTime = sriLankanTime.format(DateTimeFormatter.ofPattern("MMMM d, yyyy, h:mm:ss a"));
                
                log.info("Notifications sent to user {} for {} nearby merchants on page {} at {}.",
                        userId, merchantIdList.size(), currentPage, humanReadableTime);
            } else {
                log.info("No deals found for {} merchants (userId: {}, page: {}, location: [{}, {}]). Skipping notification.",
                        merchantIdList.size(), userId, currentPage, latitude, longitude);
            }

            currentPage++;
            hasMorePages = currentPage < totalPage;
        }
    }

    /**
     * Gets merchant deals for specific merchants only (used for preference changes).
     * This method only searches for the newly added preferred merchants.
     *
     * @param latitude latitude
     * @param longitude longitude
     * @param userId user ID
     * @param specificMerchantIds list of specific merchant IDs to search for
     */
    public void getMerchantIdListForSpecificMerchants(double latitude, double longitude, 
                                                     String userId, List<String> specificMerchantIds, String originalMessageTime) {
        
        log.info("Searching for specific merchants for user {}: {}", userId, specificMerchantIds);

        if (specificMerchantIds.isEmpty()) {
            log.info("No specific merchants to search for user {}. Skipping notification.", userId);
            return;
        }

        int currentPage = FIRST_PAGE;
        int totalPage;
        boolean hasMorePages = true;

        while (hasMorePages) {
            Pageable pageable = PageRequest.of(currentPage, merchantSize);

            // Search for specific merchants only
            Page<String> merchantIdListByLocation = branchService.getSpecificMerchantIdListByLocation(
                    pageable, latitude, longitude, searchRadiusKm, specificMerchantIds
            );

            log.debug("Page {} → found {} specific merchants (within {} km).",
                    currentPage,
                    merchantIdListByLocation.getTotalElements(),
                    searchRadiusKm);

            if (!merchantIdListByLocation.hasContent()) {
                log.info("No specific merchants found for user {} at location (lat: {}, lon: {}).",
                        userId, latitude, longitude);
                break;
            }

            totalPage = merchantIdListByLocation.getTotalPages();
            List<String> merchantIdList = merchantIdListByLocation.getContent();

            log.info("Found {} specific merchants for user {} on page {}.",
                    merchantIdList.size(), userId, currentPage);

            // Fetch bulk deals for these specific merchants
            BulkMerchantDealsResponseDto bulkDealsByMerchantIds = getBulkDealsByMerchantIds(merchantIdList);

            boolean hasDeals = bulkDealsByMerchantIds != null
                    && bulkDealsByMerchantIds.getDealGroups() != null
                    && !bulkDealsByMerchantIds.getDealGroups().isEmpty();

            if (hasDeals) {
                log.info("Received {} deal groups for specific merchants for user {} (page: {}, lat: {}, lon: {}).",
                        bulkDealsByMerchantIds.getDealGroups().size(),
                        userId, currentPage, latitude, longitude);

                PushNotificationRequestDto pushNotificationRequestDto =
                        new PushNotificationRequestDto(userId, bulkDealsByMerchantIds);
                notificationService.sendNotification(pushNotificationRequestDto);

                // Get current Sri Lankan time
                ZonedDateTime sriLankanTime = ZonedDateTime.now(ZoneId.of("Asia/Colombo"));
                String currentTime = sriLankanTime.format(DateTimeFormatter.ofPattern("MMMM d, yyyy, h:mm:ss a"));
                
                log.info("Notifications sent to user {} for {} specific merchants on page {} at {} (PubSub message received at: {}).",
                        userId, merchantIdList.size(), currentPage, currentTime, originalMessageTime);
            } else {
                log.info("No deals found for {} specific merchants (userId: {}, page: {}, location: [{}, {}]). Skipping notification.",
                        merchantIdList.size(), userId, currentPage, latitude, longitude);
            }

            currentPage++;
            hasMorePages = currentPage < totalPage;
        }
    }

    /**
     * This method used to get bulk deals by merchant id(s).
     *
     * @param merchantIds merchant id list
     * @return BulkMerchantDealsResponseDto
     */
    public BulkMerchantDealsResponseDto getBulkDealsByMerchantIds(List<String> merchantIds) {
        HttpHeaders headers = getAuthHeaders();
        var bulkUserRequestDto = new BulkUserRequestDto(merchantIds);
        HttpEntity<BulkUserRequestDto> entity = new HttpEntity<>(bulkUserRequestDto, headers);
        try {
            log.debug("Calling core service to get the bulk deal list. url: {},", getMerchantInfoUrl);
            ResponseEntity<BulkDealResponseWrapper> result =
                    restTemplate.exchange(getMerchantInfoUrl, HttpMethod.POST, entity, BulkDealResponseWrapper.class);
            String responseBody = Objects.requireNonNull(result.getBody()).getData().toLogJson();
            log.debug("Getting user by id was successful. statusCode: {}, response: {}",
                    result.getStatusCode(), responseBody);
            return Objects.requireNonNull(result.getBody()).getData();
        } catch (HttpClientErrorException e) {
            log.error(String.valueOf(e));
            throw new CoreServiceHttpClientErrorException(e.getStatusCode(), FAILED_TO_GET_BULK_DEALS, e);
        }
    }

    /**
     * Gets deals of the day from core service and converts to BulkMerchantDealsResponseDto format.
     * Filters deals by eligible merchant IDs.
     *
     * @param eligibleMerchantIds list of eligible merchant IDs to filter by
     * @param searchTerm search term (optional, use "ALL" if null)
     * @return BulkMerchantDealsResponseDto with deals grouped by merchant
     */
    private BulkMerchantDealsResponseDto getDealsOfTheDayByMerchantIds(
            List<String> eligibleMerchantIds, String searchTerm) {
        HttpHeaders headers = getAuthHeadersWithUserId();
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        
        String normalizedSearchTerm = (searchTerm != null && !searchTerm.trim().isEmpty()) 
                ? searchTerm.trim() : "ALL";
        String categoryId = "ALL";
        String userType = "MERCHANT";
        String merchantId = "ALL";
        int page = 0;
        int size = 250;
        
        String url = String.format("%s/%d/%d/category/%s/%s/%s/search/%s",
                getDealsOfTheDayUrl, page, size, categoryId, userType, merchantId, normalizedSearchTerm);
        
        try {
            log.debug("Calling core service to get deals of the day. url: {}", url);
            ResponseEntity<DealListResponseWrapper> result =
                    restTemplate.exchange(url, HttpMethod.GET, entity, DealListResponseWrapper.class);
            
            DealListResponseWrapper.DealListResponseData responseData = 
                    Objects.requireNonNull(result.getBody()).getData();
            
            if (responseData == null || responseData.getDeals() == null || responseData.getDeals().isEmpty()) {
                log.debug("No deals of the day found.");
                return new BulkMerchantDealsResponseDto();
            }
            
            // Convert to BulkMerchantDealsResponseDto format, filtering by eligible merchant IDs
            BulkMerchantDealsResponseDto bulkResponse = convertDealListToBulkMerchantDeals(
                    responseData.getDeals(), eligibleMerchantIds);
            
            log.debug("Successfully retrieved {} deal groups from deals of the day.",
                    bulkResponse.getDealGroups().size());
            return bulkResponse;
        } catch (HttpClientErrorException e) {
            log.error("Failed to get deals of the day. Status: {}, Error: {}", 
                    e.getStatusCode(), e.getMessage());
            throw new CoreServiceHttpClientErrorException(e.getStatusCode(), 
                    "Failed to get deals of the day from calling core service.", e);
        }
    }

    /**
     * Gets recently expiring deals from core service and converts to BulkMerchantDealsResponseDto format.
     * Filters deals by eligible merchant IDs.
     *
     * @param eligibleMerchantIds list of eligible merchant IDs to filter by
     * @param searchTerm search term (optional, use "ALL" if null)
     * @return BulkMerchantDealsResponseDto with deals grouped by merchant
     */
    private BulkMerchantDealsResponseDto getRecentlyExpiringDealsByMerchantIds(
            List<String> eligibleMerchantIds, String searchTerm) {
        HttpHeaders headers = getAuthHeadersWithUserId();
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        
        String normalizedSearchTerm = (searchTerm != null && !searchTerm.trim().isEmpty()) 
                ? searchTerm.trim() : "ALL";
        String categoryId = "ALL";
        String userType = "MERCHANT";
        String merchantId = "ALL";
        int page = 0;
        int size = 250;
        
        String url = String.format("%s/%d/%d/category/%s/%s/%s/search/%s",
                getRecentlyExpireDealsUrl, page, size, categoryId, userType, merchantId, normalizedSearchTerm);
        
        try {
            log.debug("Calling core service to get recently expiring deals. url: {}", url);
            ResponseEntity<DealListResponseWrapper> result =
                    restTemplate.exchange(url, HttpMethod.GET, entity, DealListResponseWrapper.class);
            
            DealListResponseWrapper.DealListResponseData responseData = 
                    Objects.requireNonNull(result.getBody()).getData();
            
            if (responseData == null || responseData.getDeals() == null || responseData.getDeals().isEmpty()) {
                log.debug("No recently expiring deals found.");
                return new BulkMerchantDealsResponseDto();
            }
            
            // Convert to BulkMerchantDealsResponseDto format, filtering by eligible merchant IDs
            BulkMerchantDealsResponseDto bulkResponse = convertDealListToBulkMerchantDeals(
                    responseData.getDeals(), eligibleMerchantIds);
            
            log.debug("Successfully retrieved {} deal groups from recently expiring deals.",
                    bulkResponse.getDealGroups().size());
            return bulkResponse;
        } catch (HttpClientErrorException e) {
            log.error("Failed to get recently expiring deals. Status: {}, Error: {}", 
                    e.getStatusCode(), e.getMessage());
            throw new CoreServiceHttpClientErrorException(e.getStatusCode(), 
                    FAILED_TO_GET_RECENTLY_EXPIRE_DEALS, e);
        }
    }

    /**
     * Converts a list of deal maps (from JSON response) to BulkMerchantDealsResponseDto format,
     * grouping deals by merchant and filtering by eligible merchant IDs.
     *
     * @param deals list of deals as maps from core service JSON response
     * @param eligibleMerchantIds list of eligible merchant IDs to filter by
     * @return BulkMerchantDealsResponseDto with deals grouped by merchant
     */
    private BulkMerchantDealsResponseDto convertDealListToBulkMerchantDeals(
            List<Map<String, Object>> deals,
            List<String> eligibleMerchantIds) {
        
        Map<String, MerchantDealsResponseDto> merchantDealsMap = new HashMap<>();
        Set<String> eligibleMerchantSet = new HashSet<>(eligibleMerchantIds);
        
        for (Map<String, Object> dealMap : deals) {
            String merchantId = extractMerchantId(dealMap);
            if (merchantId == null || !eligibleMerchantSet.contains(merchantId)) {
                continue;
            }
            
            MerchantDealsResponseDto merchantDeals = merchantDealsMap.computeIfAbsent(
                    merchantId, id -> createMerchantDealsGroup(dealMap));
            
            NotificationDealResponseDto notificationDeal = convertToNotificationDeal(dealMap);
            merchantDeals.getDeals().add(notificationDeal);
        }
        
        BulkMerchantDealsResponseDto bulkResponse = new BulkMerchantDealsResponseDto();
        bulkResponse.setDealGroups(new ArrayList<>(merchantDealsMap.values()));
        return bulkResponse;
    }

    /**
     * Extracts merchant ID from deal map (checks both merchant and bank fields).
     *
     * @param dealMap deal map from JSON response
     * @return merchant ID or null if not found
     */
    private String extractMerchantId(Map<String, Object> dealMap) {
        Map<String, Object> merchantMap = (Map<String, Object>) dealMap.get("merchant");
        if (merchantMap != null) {
            return extractIdFromMap(merchantMap);
        }
        
        Map<String, Object> bankMap = (Map<String, Object>) dealMap.get("bank");
        if (bankMap != null) {
            return extractIdFromMap(bankMap);
        }
        
        return null;
    }

    /**
     * Extracts ID from a map as a string.
     *
     * @param map map containing id field
     * @return ID as string or null
     */
    private String extractIdFromMap(Map<String, Object> map) {
        Object idObj = map.get("id");
        return idObj != null ? idObj.toString() : null;
    }

    /**
     * Creates a new MerchantDealsResponseDto group with merchant information.
     *
     * @param dealMap deal map containing merchant/bank information
     * @return new MerchantDealsResponseDto instance
     */
    private MerchantDealsResponseDto createMerchantDealsGroup(Map<String, Object> dealMap) {
        MerchantDealsResponseDto newGroup = new MerchantDealsResponseDto();
        MerchantBusinessResponseDto merchantDto = createMerchantDto(dealMap);
        newGroup.setMerchant(merchantDto);
        newGroup.setDeals(new ArrayList<>());
        return newGroup;
    }

    /**
     * Creates MerchantBusinessResponseDto from deal map.
     *
     * @param dealMap deal map containing merchant/bank information
     * @return MerchantBusinessResponseDto instance
     */
    private MerchantBusinessResponseDto createMerchantDto(Map<String, Object> dealMap) {
        MerchantBusinessResponseDto merchantDto = new MerchantBusinessResponseDto();
        Map<String, Object> merchantInfo = getMerchantInfo(dealMap);
        
        if (merchantInfo != null) {
            merchantDto.setId(extractIdFromMap(merchantInfo));
            merchantDto.setName(extractStringFromMap(merchantInfo, "name"));
            merchantDto.setImageUrl(extractStringFromMap(merchantInfo, "imageUrl"));
        }
        
        return merchantDto;
    }

    /**
     * Gets merchant info map from deal map (checks merchant first, then bank).
     *
     * @param dealMap deal map from JSON response
     * @return merchant info map or null
     */
    private Map<String, Object> getMerchantInfo(Map<String, Object> dealMap) {
        Map<String, Object> merchantMap = (Map<String, Object>) dealMap.get("merchant");
        if (merchantMap != null) {
            return merchantMap;
        }
        return (Map<String, Object>) dealMap.get("bank");
    }

    /**
     * Extracts string value from map.
     *
     * @param map map to extract from
     * @param key key to extract
     * @return string value or null
     */
    private String extractStringFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * Converts deal map to NotificationDealResponseDto.
     *
     * @param dealMap deal map from JSON response
     * @return NotificationDealResponseDto instance
     */
    private NotificationDealResponseDto convertToNotificationDeal(Map<String, Object> dealMap) {
        UserType dealSource = extractDealSource(dealMap);
        Boolean isDealOfTheDay = extractBooleanValue(dealMap, "dealOfTheDay");
        Long expiredOn = extractExpiredOn(dealMap);
        
        return new NotificationDealResponseDto(
                extractStringFromMap(dealMap, "id"),
                extractStringFromMap(dealMap, "title"),
                extractStringFromMap(dealMap, "subTitle"),
                extractStringFromMap(dealMap, "coverImage"),
                dealSource,
                isDealOfTheDay,
                expiredOn
        );
    }

    /**
     * Extracts deal source (UserType) from deal map.
     *
     * @param dealMap deal map from JSON response
     * @return UserType, defaults to MERCHANT if invalid
     */
    private UserType extractDealSource(Map<String, Object> dealMap) {
        Object dealSourceObj = dealMap.get("dealSource");
        if (dealSourceObj == null) {
            return UserType.MERCHANT;
        }
        
        try {
            return UserType.valueOf(dealSourceObj.toString());
        } catch (IllegalArgumentException e) {
            return UserType.MERCHANT;
        }
    }

    /**
     * Extracts boolean value from deal map.
     *
     * @param dealMap deal map from JSON response
     * @param key key to extract
     * @return boolean value or false
     */
    private Boolean extractBooleanValue(Map<String, Object> dealMap, String key) {
        Object value = dealMap.get(key);
        return value != null && Boolean.TRUE.equals(value);
    }

    /**
     * Extracts expiredOn timestamp from deal map (DateResponseDto structure).
     *
     * @param dealMap deal map from JSON response
     * @return expiredOn timestamp in milliseconds or null
     */
    private Long extractExpiredOn(Map<String, Object> dealMap) {
        Map<String, Object> expiredOnMap = (Map<String, Object>) dealMap.get("expiredOn");
        if (expiredOnMap == null) {
            return null;
        }
        
        Object millisecondsObj = expiredOnMap.get("milliseconds");
        if (millisecondsObj instanceof Number) {
            return ((Number) millisecondsObj).longValue();
        }
        
        return null;
    }

    /**
     * Gets merchant deals with branch locations for a given location.
     * This method searches for all merchants within the configured deals search radius (from application.yml)
     * and returns their deals with branch locations.
     *
     * @param latitude latitude
     * @param longitude longitude
     * @return MerchantDealsWithLocationListResponseDto containing deals, branch locations, and search radius
     */
    public MerchantDealsWithLocationListResponseDto getMerchantDealsWithLocations(double latitude, double longitude) {
        log.info("Getting merchant deals with locations for lat: {}, lon: {}, radius: {} km", 
                latitude, longitude, dealsSearchRadiusKm);

        List<MerchantDealsWithLocationResponseDto> result = new ArrayList<>();
        int currentPage = FIRST_PAGE;
        boolean hasMorePages = true;

        while (hasMorePages) {
            Pageable pageable = PageRequest.of(currentPage, merchantSize);

            // Get all merchant IDs within radius (not just preferred)
            Page<String> merchantIdListByLocation = branchService.getAllMerchantIdListByLocation(
                    pageable, latitude, longitude, dealsSearchRadiusKm
            );

            log.debug("Page {} → found {} nearby merchants (within {} km).",
                    currentPage,
                    merchantIdListByLocation.getTotalElements(),
                    dealsSearchRadiusKm);

            if (!merchantIdListByLocation.hasContent()) {
                log.info("No nearby merchants found at location (lat: {}, lon: {}).",
                        latitude, longitude);
                break;
            }

            int totalPage = merchantIdListByLocation.getTotalPages();

            processMerchantDealsPage(latitude, longitude, merchantIdListByLocation, result, currentPage);

            currentPage++;
            hasMorePages = currentPage < totalPage;
        }

        // Sort merchants by nearest branch distance
        result.sort(Comparator.comparing(
                dealsWithLocation -> getNearestDistance(dealsWithLocation.getBranchLocations())));

        // Create response with radius included
        MerchantDealsWithLocationListResponseDto response = new MerchantDealsWithLocationListResponseDto();
        response.setDeals(result);
        response.setSearchRadiusKm(dealsSearchRadiusKm);

        log.info("Returning {} merchant deals with locations for lat: {}, lon: {} (radius: {} km)", 
                result.size(), latitude, longitude, dealsSearchRadiusKm);
        return response;
    }

    /**
     * Gets merchant deals count with branch locations for a given location with pagination.
     * This method searches for merchants within the specified radius and returns deal count per merchant
     * instead of the full deals list, along with merchant categories.
     *
     * @param latitude latitude
     * @param longitude longitude
     * @param rangeKm search radius in kilometers (optional, defaults to dealsSearchRadiusKm)
     * @param page page number (0-indexed)
     * @param size page size
     * @return MerchantDealsCountWithLocationListResponseDto containing merchants with deal counts, categories, branch locations, and pagination info
     */
    public MerchantDealsCountWithLocationListResponseDto getMerchantDealsCountWithLocations(
            double latitude, double longitude, Double rangeKm, Integer page, Integer size) {

        double searchRadius = rangeKm != null ? rangeKm : dealsSearchRadiusKm;
        int pageNumber = page != null ? page : FIRST_PAGE;
        int pageSize = size != null ? size : merchantSize;

        log.info("Getting merchant deals count with locations for lat: {}, lon: {}, radius: {} km, page: {}, size: {}",
                latitude, longitude, searchRadius, pageNumber, pageSize);

        // Collect all merchants with at least one deal across all nearby merchants
        List<MerchantDealsCountWithLocationResponseDto> allMerchantsWithDeals =
                collectMerchantsWithDealsForCount(latitude, longitude, searchRadius);

        // If no merchants with deals found
        if (allMerchantsWithDeals.isEmpty()) {
            log.info("No merchants with deals found for lat: {}, lon: {} within radius: {} km",
                    latitude, longitude, searchRadius);

            MerchantDealsCountWithLocationListResponseDto response = new MerchantDealsCountWithLocationListResponseDto();
            response.setDeals(new ArrayList<>());
            response.setSearchRadiusKm(searchRadius);
            response.setPage(pageNumber);
            response.setSize(pageSize);
            response.setTotalElements(0L);
            response.setTotalPages(0);
            return response;
        }

        // Sort merchants by nearest branch distance
        allMerchantsWithDeals.sort(Comparator.comparing(
                dealsWithLocation -> getNearestDistance(dealsWithLocation.getBranchLocations())));

        long totalElements = allMerchantsWithDeals.size();
        int totalPages = (int) Math.ceil(totalElements / (double) pageSize);

        // Apply pagination on the sorted list of merchants with deals
        List<MerchantDealsCountWithLocationResponseDto> pagedResult;
        if (pageNumber >= totalPages) {
            pagedResult = new ArrayList<>();
        } else {
            int fromIndex = pageNumber * pageSize;
            int toIndex = (int) Math.min(fromIndex + pageSize, totalElements);
            pagedResult = new ArrayList<>(allMerchantsWithDeals.subList(fromIndex, toIndex));
        }

        MerchantDealsCountWithLocationListResponseDto response = new MerchantDealsCountWithLocationListResponseDto();
        response.setDeals(pagedResult);
        response.setSearchRadiusKm(searchRadius);
        response.setPage(pageNumber);
        response.setSize(pageSize);
        response.setTotalElements(totalElements);
        response.setTotalPages(totalPages);

        log.info("Returning {} merchant deals count with locations for lat: {}, lon: {} (radius: {} km, page: {}, size: {}, totalElements: {}, totalPages: {})",
                pagedResult.size(), latitude, longitude, searchRadius, pageNumber, pageSize, totalElements, totalPages);

        return response;
    }

    /**
     * Gets the total count of nearby active deals for a given location.
     * This method searches for all merchants within the specified radius,
     * gets their active deals, and returns the total count.
     *
     * @param latitude latitude
     * @param longitude longitude
     * @param rangeKm search radius in kilometers (optional, defaults to dealsSearchRadiusKm)
     * @return NearbyActiveDealsCountResponseDto containing total active deal count and search radius
     */
    public NearbyActiveDealsCountResponseDto getNearbyActiveDealsCount(
            double latitude, double longitude, Double rangeKm) {

        double searchRadius = rangeKm != null ? rangeKm : dealsSearchRadiusKm;

        log.info("Getting nearby active deals count for lat: {}, lon: {}, radius: {} km",
                latitude, longitude, searchRadius);

        long totalActiveDealsCount = 0;
        int currentPage = FIRST_PAGE;
        boolean hasMorePages = true;

        while (hasMorePages) {
            Pageable pageable = PageRequest.of(currentPage, merchantSize);

            // Get all merchant IDs within radius
            Page<String> merchantIdListByLocation = branchService.getAllMerchantIdListByLocation(
                    pageable, latitude, longitude, searchRadius
            );

            log.debug("Page {} → found {} nearby merchants (within {} km).",
                    currentPage,
                    merchantIdListByLocation.getTotalElements(),
                    searchRadius);

            if (!merchantIdListByLocation.hasContent()) {
                log.info("No nearby merchants found at location (lat: {}, lon: {}) for page {}.",
                        latitude, longitude, currentPage);
                break;
            }

            int totalPage = merchantIdListByLocation.getTotalPages();
            List<String> merchantIdList = merchantIdListByLocation.getContent();

            log.info("Found {} nearby merchants on page {}.",
                    merchantIdList.size(), currentPage);

            // Get business profiles and filter by approval & active status
            BulkBusinessResponseDto bulkBusinessResponse = fetchBulkBusinessResponse(merchantIdList);
            List<String> eligibleMerchantIds = getEligibleMerchantIds(bulkBusinessResponse);

            if (eligibleMerchantIds.isEmpty()) {
                log.info("No approved and active merchants found on page {}. Skipping deal retrieval.", currentPage);
                currentPage++;
                hasMorePages = currentPage < totalPage;
                continue;
            }

            // Fetch bulk deals only for approved & active merchants
            BulkMerchantDealsResponseDto bulkDealsByMerchantIds = getBulkDealsByMerchantIds(eligibleMerchantIds);

            if (hasDealGroups(bulkDealsByMerchantIds)) {
                // Count deals across all merchants in this page
                for (MerchantDealsResponseDto dealGroup : bulkDealsByMerchantIds.getDealGroups()) {
                    int dealCount = dealGroup.getDeals() != null ? dealGroup.getDeals().size() : 0;
                    totalActiveDealsCount += dealCount;
                }
                log.debug("Page {} → counted {} active deals (total so far: {})",
                        currentPage, bulkDealsByMerchantIds.getDealGroups().stream()
                                .mapToInt(dg -> dg.getDeals() != null ? dg.getDeals().size() : 0)
                                .sum(), totalActiveDealsCount);
            } else {
                log.info("No deals found for {} merchants on page {}.",
                        merchantIdList.size(), currentPage);
            }

            currentPage++;
            hasMorePages = currentPage < totalPage;
        }

        NearbyActiveDealsCountResponseDto response = new NearbyActiveDealsCountResponseDto();
        response.setActiveDealsCount(totalActiveDealsCount);
        response.setSearchRadiusKm(searchRadius);

        log.info("Returning total active deals count: {} for lat: {}, lon: {} (radius: {} km)",
                totalActiveDealsCount, latitude, longitude, searchRadius);

        return response;
    }

    /**
     * Gets categories for multiple merchants in bulk by calling core service.
     *
     * @param merchantIds list of merchant IDs
     * @return Map of merchant ID to list of categories
     */
    private Map<String, List<BasicCategoryResponseDto>> getBulkCategoriesForMerchants(List<String> merchantIds) {
        if (merchantIds == null || merchantIds.isEmpty()) {
            return new java.util.HashMap<>();
        }

        try {
            return requestBulkCategories(merchantIds);
        } catch (HttpClientErrorException e) {
            log.error("Failed to get bulk categories for merchants. statusCode: {}, body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString(), e);
            return buildEmptyCategoriesMap(merchantIds);
        } catch (Exception e) {
            log.error("Unexpected error getting bulk categories for merchants", e);
            return buildEmptyCategoriesMap(merchantIds);
        }
    }

    /**
     * Performs the actual REST call to core service to get bulk categories.
     */
    private Map<String, List<BasicCategoryResponseDto>> requestBulkCategories(List<String> merchantIds) {
        HttpHeaders headers = getAuthHeaders();
        var bulkUserRequestDto = new BulkUserRequestDto(merchantIds);
        HttpEntity<BulkUserRequestDto> entity = new HttpEntity<>(bulkUserRequestDto, headers);

        log.info("Calling core service to get bulk categories for {} merchants. url: {}", merchantIds.size(), getBulkCategoriesUrl);
        ResponseEntity<BulkMerchantCategoriesResponseWrapper> result =
                restTemplate.exchange(getBulkCategoriesUrl, HttpMethod.POST, entity, BulkMerchantCategoriesResponseWrapper.class);

        log.info("Core service responded with status: {}", result.getStatusCode());

        BulkMerchantCategoriesResponseWrapper body = result.getBody();
        if (body == null || body.getData() == null) {
            log.warn("Core service returned null body or data for bulk categories");
            return buildEmptyCategoriesMap(merchantIds);
        }

        BulkMerchantCategoriesResponseDto responseData = body.getData();

        Map<String, List<BasicCategoryResponseDto>> merchantCategories =
                responseData.getMerchantCategories() != null
                        ? responseData.getMerchantCategories()
                        : new java.util.HashMap<>();

        log.info("Getting bulk categories was successful. statusCode: {}, merchants in response: {}, categories map size: {}",
                result.getStatusCode(),
                merchantCategories.size(),
                merchantCategories.size());

        merchantCategories.forEach((merchantId, categories) ->
                log.info("Merchant {} has {} categories in response", merchantId,
                        categories != null ? categories.size() : 0));

        return merchantCategories;
    }

    /**
     * Builds an empty categories map for the given merchant IDs.
     */
    private Map<String, List<BasicCategoryResponseDto>> buildEmptyCategoriesMap(List<String> merchantIds) {
        Map<String, List<BasicCategoryResponseDto>> emptyResult = new java.util.HashMap<>();
        for (String merchantId : merchantIds) {
            emptyResult.put(merchantId, new ArrayList<>());
        }
        return emptyResult;
    }

    /**
     * This method returns header for getBulkMerchant url.
     *
     * @return headers
     */
    private HttpHeaders getAuthHeaders() {
        var headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(TIME_ZONE_HEADER, TIME_ZONE_VALUE);
        headers.set(APP_KEY, appKey);
        return headers;
    }

    /**
     * Gets auth headers with User-Id header for endpoints that require it.
     *
     * @return HttpHeaders with User-Id header
     */
    private HttpHeaders getAuthHeadersWithUserId() {
        var headers = getAuthHeaders();
        headers.set(USER_ID_HEADER, INTERNAL_USER_ID);
        return headers;
    }

    /**
     * Helper to get the nearest distance from a list of branch locations.
     *
     * @param locations branch locations
     * @return smallest non-null distanceKm, or Double.MAX_VALUE if none
     */
    private double getNearestDistance(List<LocationResponseDto> locations) {
        if (locations == null || locations.isEmpty()) {
            return Double.MAX_VALUE;
        }
        return locations.stream()
                .map(LocationResponseDto::getDistanceKm)
                .filter(Objects::nonNull)
                .min(Double::compareTo)
                .orElse(Double.MAX_VALUE);
    }

    /**
     * Fetches bulk business profile information for a list of merchant IDs.
     */
    private BulkBusinessResponseDto fetchBulkBusinessResponse(List<String> merchantIdList) {
        BulkBusinessProfileRequestDto bulkBusinessProfileRequestDto =
                new BulkBusinessProfileRequestDto(merchantIdList);

        return authUserService.getBulkBusinessResponse(
                null,
                bulkBusinessProfileRequestDto,
                UserType.MERCHANT
        );
    }

    /**
     * Fetches and groups branches by merchant for given merchant IDs within the specified radius.
     */
    private Map<String, List<Branch>> getBranchesByMerchant(double latitude,
                                                            double longitude,
                                                            double searchRadius,
                                                            List<String> eligibleMerchantIds) {
        List<Branch> branches = branchService.getBranchesByMerchantIdsAndLocation(
                latitude, longitude, searchRadius, eligibleMerchantIds
        );

        return branches.stream()
                .collect(Collectors.groupingBy(Branch::getUserId));
    }

    /**
     * Populates deal counts and merchant map from bulk deals response.
     */
    private void populateDealCountsAndMerchantMap(BulkMerchantDealsResponseDto bulkDealsByMerchantIds,
                                                  Map<String, Integer> dealCountByMerchantId,
                                                  Map<String, MerchantBusinessResponseDto> merchantMap) {
        if (!hasDealGroups(bulkDealsByMerchantIds)) {
            return;
        }

        for (MerchantDealsResponseDto dealGroup : bulkDealsByMerchantIds.getDealGroups()) {
            String merchantId = dealGroup.getMerchant().getId();
            int dealCount = dealGroup.getDeals() != null ? dealGroup.getDeals().size() : 0;
            dealCountByMerchantId.put(merchantId, dealCount);
            merchantMap.put(merchantId, dealGroup.getMerchant());
        }
    }

    /**
     * Fetches categories for a list of eligible merchants and logs summary.
     */
    private Map<String, List<BasicCategoryResponseDto>> getCategoriesForEligibleMerchants(
            List<String> eligibleMerchantIds, int currentPage) {
        log.debug("Fetching categories for {} eligible merchants on DB page {}", eligibleMerchantIds.size(), currentPage);
        Map<String, List<BasicCategoryResponseDto>> categoriesByMerchantId =
                getBulkCategoriesForMerchants(eligibleMerchantIds);
        log.debug("Retrieved categories for {} merchants on DB page {}", categoriesByMerchantId.size(), currentPage);
        return categoriesByMerchantId;
    }

    /**
     * Builds a list of nearest branch locations (only one per merchant, sorted by distance).
     */
    private List<LocationResponseDto> getNearestBranchLocations(double latitude,
                                                                double longitude,
                                                                List<Branch> merchantBranches) {
        return merchantBranches.stream()
                .map(branch -> new LocationResponseDto(branch, latitude, longitude))
                .sorted(Comparator.comparing(
                        loc -> loc.getDistanceKm() != null ? loc.getDistanceKm() : Double.MAX_VALUE))
                .limit(1)
                .collect(Collectors.toList());
    }

    /**
     * Collects all nearby merchants that have at least one deal, including their nearest branch and categories.
     */
    private List<MerchantDealsCountWithLocationResponseDto> collectMerchantsWithDealsForCount(
            double latitude, double longitude, double searchRadius) {

        List<MerchantDealsCountWithLocationResponseDto> allMerchantsWithDeals = new ArrayList<>();

        int currentPage = FIRST_PAGE;
        boolean hasMorePages = true;

        while (hasMorePages) {
            Pageable pageable = PageRequest.of(currentPage, merchantSize);

            Page<String> merchantIdListByLocation = branchService.getAllMerchantIdListByLocation(
                    pageable, latitude, longitude, searchRadius
            );

            log.debug("DB Page {} → found {} nearby merchants (within {} km).",
                    currentPage,
                    merchantIdListByLocation.getNumberOfElements(),
                    searchRadius);

            if (!merchantIdListByLocation.hasContent()) {
                log.info("No nearby merchants found at location (lat: {}, lon: {}) for DB page {}.",
                        latitude, longitude, currentPage);
                break;
            }

            int totalDbPages = merchantIdListByLocation.getTotalPages();

            allMerchantsWithDeals.addAll(
                    collectMerchantsWithDealsFromPage(latitude, longitude, searchRadius,
                            merchantIdListByLocation, currentPage));

            currentPage++;
            hasMorePages = currentPage < totalDbPages;
        }

        return allMerchantsWithDeals;
    }

    /**
     * Collects merchants with deals for a single DB page of merchant IDs.
     */
    private List<MerchantDealsCountWithLocationResponseDto> collectMerchantsWithDealsFromPage(
            double latitude,
            double longitude,
            double searchRadius,
            Page<String> merchantIdListByLocation,
            int currentPage) {

        List<MerchantDealsCountWithLocationResponseDto> pageMerchants = new ArrayList<>();

        List<String> merchantIdList = merchantIdListByLocation.getContent();

        log.info("Found {} nearby merchants on DB page {}.",
                merchantIdList.size(), currentPage);

        BulkBusinessResponseDto bulkBusinessResponse = fetchBulkBusinessResponse(merchantIdList);
        List<String> eligibleMerchantIds = getEligibleMerchantIds(bulkBusinessResponse);

        if (eligibleMerchantIds.isEmpty()) {
            log.info("No approved and active merchants found on DB page {}. Skipping deal retrieval.", currentPage);
            return pageMerchants;
        }

        BulkMerchantDealsResponseDto bulkDealsByMerchantIds = getBulkDealsByMerchantIds(eligibleMerchantIds);

        if (!hasDealGroups(bulkDealsByMerchantIds)) {
            log.info("No deals found for {} merchants on page {}.",
                    merchantIdList.size(), currentPage);
            return pageMerchants;
        }

        Map<String, List<Branch>> branchesByMerchantId =
                getBranchesByMerchant(latitude, longitude, searchRadius, eligibleMerchantIds);

        Map<String, Integer> dealCountByMerchantId = new java.util.HashMap<>();
        Map<String, MerchantBusinessResponseDto> merchantMap = new java.util.HashMap<>();
        populateDealCountsAndMerchantMap(bulkDealsByMerchantIds, dealCountByMerchantId, merchantMap);

        Map<String, List<BasicCategoryResponseDto>> categoriesByMerchantId =
                getCategoriesForEligibleMerchants(eligibleMerchantIds, currentPage);

        for (String merchantId : eligibleMerchantIds) {
            List<Branch> merchantBranches = branchesByMerchantId.getOrDefault(merchantId, new ArrayList<>());

            List<LocationResponseDto> branchLocations = getNearestBranchLocations(latitude, longitude, merchantBranches);
            if (branchLocations.isEmpty()) {
                continue;
            }

            MerchantBusinessResponseDto merchant =
                    resolveMerchantForCount(bulkBusinessResponse, merchantMap, merchantId);
            if (merchant == null) {
                continue;
            }

            int dealCount = dealCountByMerchantId.getOrDefault(merchantId, 0);
            if (dealCount <= 0) {
                continue;
            }

            List<BasicCategoryResponseDto> categories =
                    categoriesByMerchantId.getOrDefault(merchantId, new ArrayList<>());
            log.debug("Merchant {} has {} categories", merchantId, categories.size());

            MerchantBusinessWithCategoriesResponseDto merchantWithCategories =
                    new MerchantBusinessWithCategoriesResponseDto(merchant, categories);

            MerchantDealsCountWithLocationResponseDto dealsCountWithLocation =
                    new MerchantDealsCountWithLocationResponseDto();
            dealsCountWithLocation.setMerchant(merchantWithCategories);
            dealsCountWithLocation.setDealCount(dealCount);
            dealsCountWithLocation.setBranchLocations(branchLocations);

            pageMerchants.add(dealsCountWithLocation);
        }

        return pageMerchants;
    }

    /**
     * Resolves merchant info for count response, preferring data from deal groups, then falling back to business profile.
     */
    private MerchantBusinessResponseDto resolveMerchantForCount(BulkBusinessResponseDto bulkBusinessResponse,
                                                                Map<String, MerchantBusinessResponseDto> merchantMap,
                                                                String merchantId) {
        MerchantBusinessResponseDto merchant = merchantMap.get(merchantId);
        if (merchant != null) {
            return merchant;
        }

        if (bulkBusinessResponse == null || bulkBusinessResponse.getMerchants() == null) {
            return null;
        }

        BusinessResponseDto businessResponse = bulkBusinessResponse.getMerchants().stream()
                .filter(m -> m.getId().equals(merchantId))
                .findFirst()
                .orElse(null);

        if (businessResponse == null) {
            return null;
        }

        MerchantBusinessResponseDto mb = new MerchantBusinessResponseDto();
        mb.setId(businessResponse.getId());
        mb.setName(businessResponse.getName());
        mb.setImageUrl(businessResponse.getImageUrl());
        mb.setApprovalStatus(businessResponse.getApprovalStatus());
        mb.setActive(businessResponse.isActive());
        mb.setUserType(businessResponse.getUserType());
        return mb;
    }

    /**
     * Processes a single page of merchant IDs to build deals with branch locations.
     *
     * @param latitude                  user latitude
     * @param longitude                 user longitude
     * @param merchantIdListByLocation  page of merchant IDs
     * @param result                    accumulator list for results
     * @param currentPage               current page index (for logging)
     */
    private void processMerchantDealsPage(double latitude,
                                          double longitude,
                                          Page<String> merchantIdListByLocation,
                                          List<MerchantDealsWithLocationResponseDto> result,
                                          int currentPage) {

        List<String> merchantIdList = merchantIdListByLocation.getContent();

        log.info("Found {} nearby merchants on page {}.",
                merchantIdList.size(), currentPage);

        // Call Auth service to get business profiles and filter by approval & active status
        BulkBusinessProfileRequestDto bulkBusinessProfileRequestDto =
                new BulkBusinessProfileRequestDto(merchantIdList);

        BulkBusinessResponseDto bulkBusinessResponse =
                authUserService.getBulkBusinessResponse(
                        null,
                        bulkBusinessProfileRequestDto,
                        UserType.MERCHANT
                );

        List<String> eligibleMerchantIds = getEligibleMerchantIds(bulkBusinessResponse);

        if (eligibleMerchantIds.isEmpty()) {
            log.info("No approved and active merchants found on page {}. Skipping deal retrieval.", currentPage);
            return;
        }

        // Fetch bulk deals only for approved & active merchants
        BulkMerchantDealsResponseDto bulkDealsByMerchantIds = getBulkDealsByMerchantIds(eligibleMerchantIds);

        if (!hasDealGroups(bulkDealsByMerchantIds)) {
            log.info("No deals found for {} merchants on page {}.",
                    merchantIdList.size(), currentPage);
            return;
        }

        // Get branches for these (filtered) merchants within the radius
        List<Branch> branches = branchService.getBranchesByMerchantIdsAndLocation(
                latitude, longitude, dealsSearchRadiusKm, eligibleMerchantIds
        );

        // Group branches by merchant ID
        Map<String, List<Branch>> branchesByMerchantId = branches.stream()
                .collect(Collectors.groupingBy(Branch::getUserId));

        // Convert to response DTOs with branch locations (including distance from user)
        List<MerchantDealsWithLocationResponseDto> pageResults =
                buildDealsWithLocations(bulkDealsByMerchantIds, branchesByMerchantId, latitude, longitude);

        result.addAll(pageResults);

        log.info("Processed {} deal groups with branch locations on page {}.",
                bulkDealsByMerchantIds.getDealGroups().size(), currentPage);
    }

    /**
     * Extracts eligible merchant IDs from bulk business response.
     */
    private List<String> getEligibleMerchantIds(BulkBusinessResponseDto bulkBusinessResponse) {
        if (bulkBusinessResponse == null || bulkBusinessResponse.getMerchants() == null) {
            return Collections.emptyList();
        }

        return bulkBusinessResponse.getMerchants().stream()
                .filter(merchant -> ApprovalStatus.APPROVED.getMessage()
                        .equalsIgnoreCase(merchant.getApprovalStatus()))
                .filter(BusinessResponseDto::isActive)
                .map(BusinessResponseDto::getId)
                .collect(Collectors.toList());
    }

    /**
     * Checks whether bulk deals response has any deal groups.
     */
    private boolean hasDealGroups(BulkMerchantDealsResponseDto bulkDealsByMerchantIds) {
        return bulkDealsByMerchantIds != null
                && bulkDealsByMerchantIds.getDealGroups() != null
                && !bulkDealsByMerchantIds.getDealGroups().isEmpty();
    }

    /**
     * Builds deal-with-location DTOs for each merchant, including distance information.
     */
    private List<MerchantDealsWithLocationResponseDto> buildDealsWithLocations(
            BulkMerchantDealsResponseDto bulkDealsByMerchantIds,
            Map<String, List<Branch>> branchesByMerchantId,
            double latitude,
            double longitude) {

        List<MerchantDealsWithLocationResponseDto> results = new ArrayList<>();

        for (MerchantDealsResponseDto dealGroup : bulkDealsByMerchantIds.getDealGroups()) {
            String merchantId = dealGroup.getMerchant().getId();
            List<Branch> merchantBranches = branchesByMerchantId.getOrDefault(merchantId, new ArrayList<>());

            // Convert branches to LocationResponseDto with distance and sort by nearest first
            List<LocationResponseDto> branchLocations = merchantBranches.stream()
                    .map(branch -> new LocationResponseDto(branch, latitude, longitude))
                    .sorted(Comparator.comparing(
                            loc -> loc.getDistanceKm() != null ? loc.getDistanceKm() : Double.MAX_VALUE))
                    .collect(Collectors.toList());

            MerchantDealsWithLocationResponseDto dealsWithLocation = new MerchantDealsWithLocationResponseDto();
            dealsWithLocation.setMerchant(dealGroup.getMerchant());
            dealsWithLocation.setDeals(dealGroup.getDeals());
            dealsWithLocation.setBranchLocations(branchLocations);

            results.add(dealsWithLocation);
        }
        return results;
    }

    /**
     * Gets nearby merchants with active deals count and distance for a given location.
     * This method searches for merchants within the configured search radius and returns
     * merchants with their categories, distance, and active deals count.
     * Only merchants with at least one active deal are returned.
     *
     * @param latitude latitude
     * @param longitude longitude
     * @param rangeKm search radius in kilometers (optional, defaults to searchRadiusKm)
     * @param searchTerm search term to filter merchants by name (optional)
     * @param page page number (0-indexed, optional, defaults to 0)
     * @param size page size (optional, defaults to merchantSize)
     * @return MerchantListWithDistanceAndDealsResponseDto containing merchants with distance and active deals count
     */
    public MerchantListWithDistanceAndDealsResponseDto getNearbyMerchantsWithActiveDeals(
            double latitude, double longitude, Double rangeKm, String searchTerm, Integer page, Integer size) {

        MerchantParams params = new MerchantParams(
                rangeKm != null ? rangeKm : searchRadiusKm,
                normalizeSearchTerm(searchTerm),
                page != null ? page : FIRST_PAGE,
                size != null ? size : merchantSize
        );

        log.info("Getting nearby merchants with active deals for lat: {}, lon: {}, radius: {} km, searchTerm: {}, page: {}, size: {}",
                latitude, longitude, params.searchRadius, params.searchTerm, params.pageNumber, params.pageSize);

        List<MerchantWithDistanceAndDealsResponseDto> merchantsWithDeals = 
                collectMerchantsWithDeals(latitude, longitude, params);

        merchantsWithDeals.sort(Comparator.comparing(
                m -> m.getDistanceKm() != null ? m.getDistanceKm() : Double.MAX_VALUE));

        PaginationResult paginationResult = applyPagination(
                merchantsWithDeals, params.pageNumber, params.pageSize);

        return buildMerchantsResponse(paginationResult, params, latitude, longitude);
    }

    /**
     * Parameters for merchant search.
     */
    private static class MerchantParams {
        final double searchRadius;
        final String searchTerm;
        final int pageNumber;
        final int pageSize;

        MerchantParams(double searchRadius, String searchTerm, int pageNumber, int pageSize) {
            this.searchRadius = searchRadius;
            this.searchTerm = searchTerm;
            this.pageNumber = pageNumber;
            this.pageSize = pageSize;
        }
    }

    /**
     * Collects merchants with active deals by iterating through merchant pages.
     */
    private List<MerchantWithDistanceAndDealsResponseDto> collectMerchantsWithDeals(
            double latitude, double longitude, MerchantParams params) {
        
        List<MerchantWithDistanceAndDealsResponseDto> merchantsWithDeals = new ArrayList<>();
        int currentPage = FIRST_PAGE;
        int totalDbPages = Integer.MAX_VALUE;

        while (currentPage < totalDbPages) {
            Pageable pageable = PageRequest.of(currentPage, merchantSize);
            Page<String> merchantIdListByLocation = branchService.getAllMerchantIdListByLocation(
                    pageable, latitude, longitude, params.searchRadius);

            log.debug("DB Page {} → found {} nearby merchants (within {} km).",
                    currentPage, merchantIdListByLocation.getNumberOfElements(), params.searchRadius);

            if (!merchantIdListByLocation.hasContent()) {
                log.info("No nearby merchants found at location (lat: {}, lon: {}) for DB page {}.",
                        latitude, longitude, currentPage);
                break;
            }

            totalDbPages = merchantIdListByLocation.getTotalPages();
            List<String> merchantIdList = merchantIdListByLocation.getContent();

            List<MerchantWithDistanceAndDealsResponseDto> pageResults = 
                    processMerchantsPage(latitude, longitude, params, merchantIdList);

            merchantsWithDeals.addAll(pageResults);
            currentPage++;
        }

        return merchantsWithDeals;
    }

    /**
     * Processes a single page of merchants and returns merchants with active deals.
     */
    private List<MerchantWithDistanceAndDealsResponseDto> processMerchantsPage(
            double latitude, double longitude, MerchantParams params, List<String> merchantIdList) {
        
        BulkBusinessResponseDto bulkBusinessResponse = fetchBulkBusinessResponse(merchantIdList);
        List<String> eligibleMerchantIds = getEligibleMerchantIds(bulkBusinessResponse);

        if (eligibleMerchantIds.isEmpty()) {
            log.info("No approved and active merchants found.");
            return new ArrayList<>();
        }

        BulkMerchantDealsResponseDto bulkDealsByMerchantIds = getBulkDealsByMerchantIds(eligibleMerchantIds);

        if (!hasDealGroups(bulkDealsByMerchantIds)) {
            log.info("No deals found for {} merchants.", eligibleMerchantIds.size());
            return new ArrayList<>();
        }

        Map<String, List<Branch>> branchesByMerchantId = 
                getBranchesByMerchant(latitude, longitude, params.searchRadius, eligibleMerchantIds);

        Map<String, Integer> dealCountByMerchantId = new HashMap<>();
        Map<String, MerchantBusinessResponseDto> merchantMap = new HashMap<>();
        populateDealCountsAndMerchantMap(bulkDealsByMerchantIds, dealCountByMerchantId, merchantMap);

        Map<String, List<BasicCategoryResponseDto>> categoriesByMerchantId = 
                getCategoriesForEligibleMerchants(eligibleMerchantIds, FIRST_PAGE);

        return buildMerchantResponseDtos(eligibleMerchantIds, dealCountByMerchantId, branchesByMerchantId,
                bulkBusinessResponse, merchantMap, categoriesByMerchantId, params.searchTerm, latitude, longitude);
    }

    /**
     * Builds merchant response DTOs for eligible merchants.
     */
    private List<MerchantWithDistanceAndDealsResponseDto> buildMerchantResponseDtos(
            List<String> eligibleMerchantIds,
            Map<String, Integer> dealCountByMerchantId,
            Map<String, List<Branch>> branchesByMerchantId,
            BulkBusinessResponseDto bulkBusinessResponse,
            Map<String, MerchantBusinessResponseDto> merchantMap,
            Map<String, List<BasicCategoryResponseDto>> categoriesByMerchantId,
            String searchTerm,
            double latitude,
            double longitude) {
        
        List<MerchantWithDistanceAndDealsResponseDto> merchantsWithDeals = new ArrayList<>();

        for (String merchantId : eligibleMerchantIds) {
            MerchantWithDistanceAndDealsResponseDto merchantDto = buildMerchantDto(
                    merchantId, dealCountByMerchantId, branchesByMerchantId,
                    bulkBusinessResponse, merchantMap, categoriesByMerchantId,
                    searchTerm, latitude, longitude);

            if (merchantDto != null) {
                merchantsWithDeals.add(merchantDto);
            }
        }

        return merchantsWithDeals;
    }

    /**
     * Builds a single merchant DTO if it meets all criteria.
     */
    private MerchantWithDistanceAndDealsResponseDto buildMerchantDto(
            String merchantId,
            Map<String, Integer> dealCountByMerchantId,
            Map<String, List<Branch>> branchesByMerchantId,
            BulkBusinessResponseDto bulkBusinessResponse,
            Map<String, MerchantBusinessResponseDto> merchantMap,
            Map<String, List<BasicCategoryResponseDto>> categoriesByMerchantId,
            String searchTerm,
            double latitude,
            double longitude) {
        
        int dealCount = dealCountByMerchantId.getOrDefault(merchantId, 0);
        if (dealCount <= 0) {
            return null;
        }

        List<Branch> merchantBranches = branchesByMerchantId.getOrDefault(merchantId, new ArrayList<>());
        if (merchantBranches.isEmpty()) {
            return null;
        }

        LocationResponseDto nearestBranch = findNearestBranch(merchantBranches, latitude, longitude);
        if (nearestBranch == null) {
            return null;
        }

        MerchantBusinessResponseDto merchant = resolveMerchantForCount(
                bulkBusinessResponse, merchantMap, merchantId);
        if (merchant == null) {
            return null;
        }

        if (!matchesSearchTerm(merchant, searchTerm)) {
            return null;
        }

        List<BasicCategoryResponseDto> categories = 
                categoriesByMerchantId.getOrDefault(merchantId, new ArrayList<>());

        return createMerchantWithDistanceAndDeals(merchant, categories, nearestBranch, dealCount);
    }

    /**
     * Finds the nearest branch to the given location.
     */
    private LocationResponseDto findNearestBranch(List<Branch> branches, double latitude, double longitude) {
        return branches.stream()
                .map(branch -> new LocationResponseDto(branch, latitude, longitude))
                .min(Comparator.comparing(
                        loc -> loc.getDistanceKm() != null ? loc.getDistanceKm() : Double.MAX_VALUE))
                .orElse(null);
    }

    /**
     * Checks if merchant matches the search term.
     */
    private boolean matchesSearchTerm(MerchantBusinessResponseDto merchant, String searchTerm) {
        if (searchTerm == null) {
            return true;
        }

        String merchantName = merchant.getName();
        if (merchantName == null) {
            return false;
        }

        return merchantName.toLowerCase().contains(searchTerm.toLowerCase());
    }

    /**
     * Creates a MerchantWithDistanceAndDealsResponseDto.
     */
    private MerchantWithDistanceAndDealsResponseDto createMerchantWithDistanceAndDeals(
            MerchantBusinessResponseDto merchant,
            List<BasicCategoryResponseDto> categories,
            LocationResponseDto nearestBranch,
            int dealCount) {
        
        MerchantBusinessWithCategoriesResponseDto merchantWithCategories = 
                new MerchantBusinessWithCategoriesResponseDto(merchant, categories);

        MerchantWithDistanceAndDealsResponseDto merchantDto = new MerchantWithDistanceAndDealsResponseDto();
        merchantDto.setMerchant(merchantWithCategories);
        merchantDto.setDistanceKm(nearestBranch.getDistanceKm());
        merchantDto.setDistanceInMeters(nearestBranch.getDistanceInMeters());
        merchantDto.setActiveDealsCount(dealCount);

        return merchantDto;
    }

    /**
     * Builds the merchants response DTO.
     */
    @SuppressWarnings("unchecked")
    private MerchantListWithDistanceAndDealsResponseDto buildMerchantsResponse(
            PaginationResult paginationResult, MerchantParams params,
            double latitude, double longitude) {
        
        List<MerchantWithDistanceAndDealsResponseDto> pagedResult = 
                (List<MerchantWithDistanceAndDealsResponseDto>) paginationResult.pagedResult;
        
        MerchantListWithDistanceAndDealsResponseDto response = 
                new MerchantListWithDistanceAndDealsResponseDto();
        response.setMerchants(pagedResult);
        response.setSearchRadiusKm(params.searchRadius);
        response.setPage(params.pageNumber);
        response.setSize(params.pageSize);
        response.setTotalElements(paginationResult.totalElements);
        response.setTotalPages(paginationResult.totalPages);

        log.info("Returning {} merchants with active deals for lat: {}, lon: {} (radius: {} km, page: {}, size: {}, totalElements: {}, totalPages: {})",
                pagedResult.size(), latitude, longitude, params.searchRadius,
                params.pageNumber, params.pageSize, paginationResult.totalElements, paginationResult.totalPages);

        return response;
    }

    /**
     * Gets nearby deals with filters (Deals of the Day, Recommended, Recently Expiring) and search term.
     * This method searches for merchants within the configured search radius and returns their deals
     * filtered by the specified filter type and search term.
     *
     * @param latitude latitude
     * @param longitude longitude
     * @param rangeKm search radius in kilometers (optional, defaults to dealsSearchRadiusKm)
     * @param searchTerm search term to filter deals by title (optional)
     * @param filterType filter type: DEALS_OF_THE_DAY, RECOMMENDED, or RECENTLY_EXPIRING
     * @param page page number (0-indexed, optional, defaults to 0)
     * @param size page size (optional, defaults to merchantSize)
     * @return MerchantDealsWithLocationListResponseDto containing filtered deals with locations and pagination
     */
    public MerchantDealsWithLocationListResponseDto getNearbyDealsWithFilters(
            double latitude, double longitude, Double rangeKm, String searchTerm,
            DealFilterType filterType, Integer page, Integer size) {

        FilterParams params = new FilterParams(
                rangeKm != null ? rangeKm : dealsSearchRadiusKm,
                normalizeSearchTerm(searchTerm),
                page != null ? page : FIRST_PAGE,
                size != null ? size : merchantSize
        );

        log.info("Getting nearby deals with filters for lat: {}, lon: {}, radius: {} km, searchTerm: {}, filterType: {}, page: {}, size: {}",
                latitude, longitude, params.searchRadius, params.searchTerm, filterType, params.pageNumber, params.pageSize);

        List<MerchantDealsWithLocationResponseDto> allDealsWithLocations =
                collectDealsWithLocations(latitude, longitude, params, filterType);

        sortDealsByFilterType(allDealsWithLocations, filterType);
        PaginationResult paginationResult = applyPagination(
                allDealsWithLocations, params.pageNumber, params.pageSize);

        return buildDealsResponse(paginationResult, params, filterType, latitude, longitude);
    }

    /**
     * Gets nearby deals with the same filters as {@link #getNearbyDealsWithFilters} but returns a flat
     * {@link DealListResponseDto} (same structure as core service's DealListResponseDto) instead of
     * merchant-grouped deals with locations.
     *
     * @param latitude   latitude
     * @param longitude  longitude
     * @param rangeKm    search radius in kilometers (optional)
     * @param searchTerm search term (optional)
     * @param filterType DEALS_OF_THE_DAY, RECOMMENDED, or RECENTLY_EXPIRING
     * @param page       page number (0-indexed, optional)
     * @param size       page size (optional)
     * @return DealListResponseDto containing filtered deals with pagination
     */
    public DealListResponseDto getNearbyDealsWithFiltersAsDealList(
            double latitude, double longitude, Double rangeKm, String searchTerm,
            DealFilterType filterType, Integer page, Integer size, String userId) {

        FilterParams params = new FilterParams(
                rangeKm != null ? rangeKm : dealsSearchRadiusKm,
                normalizeSearchTerm(searchTerm),
                page != null ? page : FIRST_PAGE,
                size != null ? size : merchantSize
        );

        log.info("Getting nearby deals as deal list for lat: {}, lon: {}, radius: {} km, searchTerm: {}, filterType: {}, page: {}, size: {}",
                latitude, longitude, params.searchRadius, params.searchTerm, filterType, params.pageNumber, params.pageSize);

        List<MerchantDealsWithLocationResponseDto> allDealsWithLocations =
                collectDealsWithLocations(latitude, longitude, params, filterType);

        sortDealsByFilterType(allDealsWithLocations, filterType);

        List<String> orderedDealIds = flattenToOrderedDealIds(allDealsWithLocations);
        long totalElements = orderedDealIds.size();

        if (orderedDealIds.isEmpty()) {
            return buildEmptyDealListResponse(params.pageNumber, params.pageSize);
        }

        int fromIndex = params.pageNumber * params.pageSize;
        if (fromIndex >= totalElements) {
            return buildEmptyDealListResponse(params.pageNumber, params.pageSize, (int) totalElements);
        }
        int toIndex = (int) Math.min(fromIndex + params.pageSize, totalElements);
        List<String> pageIds = new ArrayList<>(orderedDealIds.subList(fromIndex, toIndex));

        return getDealsByIds(pageIds, params.pageNumber, params.pageSize, totalElements, userId);
    }

    /**
     * Flattens merchant deal groups to a single ordered list of deal IDs.
     */
    private List<String> flattenToOrderedDealIds(List<MerchantDealsWithLocationResponseDto> allDealsWithLocations) {
        List<String> ids = new ArrayList<>();
        for (MerchantDealsWithLocationResponseDto group : allDealsWithLocations) {
            if (group.getDeals() != null) {
                for (NotificationDealResponseDto d : group.getDeals()) {
                    if (d != null && d.getId() != null) {
                        ids.add(d.getId());
                    }
                }
            }
        }
        return ids;
    }

    /**
     * Builds an empty DealListResponseDto for the given page and size.
     */
    private DealListResponseDto buildEmptyDealListResponse(int page, int size) {
        return buildEmptyDealListResponse(page, size, 0);
    }

    private DealListResponseDto buildEmptyDealListResponse(int page, int size, int totalElements) {
        DealListResponseDto dto = new DealListResponseDto();
        dto.setDeals(new ArrayList<>());
        dto.setTotalItems(totalElements);
        dto.setTotalPages(totalElements > 0 ? (int) Math.ceil(totalElements / (double) size) : 0);
        dto.setPage(page);
        dto.setSize(size);
        return dto;
    }

    /**
     * Calls core service to get full deal list for the given deal IDs and returns DealListResponseDto.
     * When userId is provided, core service will set the favorite flag per deal for that user.
     */
    public DealListResponseDto getDealsByIds(List<String> ids, int page, int size, long totalElements, String userId) {
        if (ids == null || ids.isEmpty()) {
            return buildEmptyDealListResponse(page, size, (int) totalElements);
        }
        HttpHeaders headers = getAuthHeaders();
        headers.set(TIME_ZONE_HEADER, TIME_ZONE_VALUE);
        if (userId != null && !userId.isBlank()) {
            headers.set(USER_ID_HEADER, userId);
        }

        String url = UriComponentsBuilder.fromHttpUrl(getDealsByIdsUrl)
                .queryParam("ids", ids)
                .queryParam("page", page)
                .queryParam("size", size)
                .queryParam("totalElements", totalElements)
                .build()
                .toUriString();

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        try {
            log.debug("Calling core service to get deals by IDs. url: {}", url);
            ResponseEntity<DealListResponseWrapper> result =
                    restTemplate.exchange(url, HttpMethod.GET, entity, DealListResponseWrapper.class);
            DealListResponseWrapper.DealListResponseData data = Objects.requireNonNull(result.getBody()).getData();
            if (data == null) {
                return buildEmptyDealListResponse(page, size, (int) totalElements);
            }
            DealListResponseDto dto = new DealListResponseDto();
            dto.setDeals(data.getDeals() != null ? data.getDeals() : new ArrayList<>());
            dto.setTotalItems(data.getTotalItems() != null ? data.getTotalItems() : 0L);
            dto.setTotalPages(data.getTotalPages() != null ? data.getTotalPages() : 0);
            dto.setPage(data.getPage() != null ? data.getPage() : page);
            dto.setSize(data.getSize() != null ? data.getSize() : size);
            return dto;
        } catch (HttpClientErrorException e) {
            log.error("Failed to get deals by IDs from core. Status: {}", e.getStatusCode(), e);
            throw new CoreServiceHttpClientErrorException(e.getStatusCode(),
                    "Failed to get deals by IDs from core service.", e);
        }
    }

    /**
     * Parameters for filtering deals.
     */
    private static class FilterParams {
        final double searchRadius;
        final String searchTerm;
        final int pageNumber;
        final int pageSize;

        FilterParams(double searchRadius, String searchTerm, int pageNumber, int pageSize) {
            this.searchRadius = searchRadius;
            this.searchTerm = searchTerm;
            this.pageNumber = pageNumber;
            this.pageSize = pageSize;
        }
    }

    /**
     * Normalizes search term (trims and returns null if empty).
     */
    private String normalizeSearchTerm(String searchTerm) {
        return searchTerm != null && !searchTerm.trim().isEmpty() ? searchTerm.trim() : null;
    }

    /**
     * Collects deals with locations by iterating through merchant pages.
     */
    private List<MerchantDealsWithLocationResponseDto> collectDealsWithLocations(
            double latitude, double longitude, FilterParams params, DealFilterType filterType) {
        
        List<MerchantDealsWithLocationResponseDto> allDealsWithLocations = new ArrayList<>();
        int currentPage = FIRST_PAGE;
        int totalDbPages = Integer.MAX_VALUE;

        while (currentPage < totalDbPages) {
            Pageable pageable = PageRequest.of(currentPage, merchantSize);
            Page<String> merchantIdListByLocation = branchService.getAllMerchantIdListByLocation(
                    pageable, latitude, longitude, params.searchRadius);

            log.debug("DB Page {} → found {} nearby merchants (within {} km).",
                    currentPage, merchantIdListByLocation.getNumberOfElements(), params.searchRadius);

            if (!merchantIdListByLocation.hasContent()) {
                log.info("No nearby merchants found at location (lat: {}, lon: {}) for DB page {}.",
                        latitude, longitude, currentPage);
                break;
            }

            totalDbPages = merchantIdListByLocation.getTotalPages();
            List<String> merchantIdList = merchantIdListByLocation.getContent();

            List<MerchantDealsWithLocationResponseDto> pageResults = 
                    processMerchantPage(latitude, longitude, params, filterType, merchantIdList);

            allDealsWithLocations.addAll(pageResults);
            currentPage++;
        }

        return allDealsWithLocations;
    }

    /**
     * Processes a single page of merchants and returns deals with locations.
     */
    private List<MerchantDealsWithLocationResponseDto> processMerchantPage(
            double latitude, double longitude, FilterParams params, 
            DealFilterType filterType, List<String> merchantIdList) {
        
        BulkBusinessResponseDto bulkBusinessResponse = fetchBulkBusinessResponse(merchantIdList);
        List<String> eligibleMerchantIds = getEligibleMerchantIds(bulkBusinessResponse);

        if (eligibleMerchantIds.isEmpty()) {
            log.info("No approved and active merchants found.");
            return new ArrayList<>();
        }

        Map<String, List<Branch>> branchesByMerchantId = 
                getBranchesByMerchant(latitude, longitude, params.searchRadius, eligibleMerchantIds);

        BulkMerchantDealsResponseDto bulkDeals = fetchDealsByFilterType(
                eligibleMerchantIds, params.searchTerm, filterType);

        if (!hasDealGroups(bulkDeals)) {
            log.info("No deals found for {} merchants.", eligibleMerchantIds.size());
            return new ArrayList<>();
        }

        // Search term is already applied in the endpoint call for RECENTLY_EXPIRING and DEALS_OF_THE_DAY
        String searchTermForFilter = (filterType == DealFilterType.RECENTLY_EXPIRING || 
                                      filterType == DealFilterType.DEALS_OF_THE_DAY) 
                ? null : params.searchTerm;

        return filterAndBuildDealsWithLocations(
                bulkDeals, branchesByMerchantId, latitude, longitude, searchTermForFilter);
    }

    /**
     * Fetches deals based on filter type.
     */
    private BulkMerchantDealsResponseDto fetchDealsByFilterType(
            List<String> eligibleMerchantIds, String searchTerm, DealFilterType filterType) {
        
        if (filterType == DealFilterType.RECENTLY_EXPIRING) {
            return getRecentlyExpiringDealsByMerchantIds(eligibleMerchantIds, searchTerm);
        }
        
        if (filterType == DealFilterType.DEALS_OF_THE_DAY) {
            return getDealsOfTheDayByMerchantIds(eligibleMerchantIds, searchTerm);
        }
        
        // For RECOMMENDED, use bulk deals endpoint
        return getBulkDealsByMerchantIds(eligibleMerchantIds);
    }

    /**
     * Sorts deals based on filter type.
     */
    private void sortDealsByFilterType(
            List<MerchantDealsWithLocationResponseDto> deals, DealFilterType filterType) {
        
        if (filterType == DealFilterType.RECENTLY_EXPIRING) {
            deals.sort(this::compareByExpirationAndDistance);
        } else {
            deals.sort(Comparator.comparing(
                    d -> getNearestDistance(d.getBranchLocations())));
        }
    }

    /**
     * Compares deals by expiration date (ascending) and then by distance.
     */
    private int compareByExpirationAndDistance(
            MerchantDealsWithLocationResponseDto a, MerchantDealsWithLocationResponseDto b) {
        
        Long expiredOnA = getEarliestExpiration(a);
        Long expiredOnB = getEarliestExpiration(b);
        
        int expiredOnCompare = expiredOnA.compareTo(expiredOnB);
        if (expiredOnCompare != 0) {
            return expiredOnCompare;
        }
        
        double distA = getNearestDistance(a.getBranchLocations());
        double distB = getNearestDistance(b.getBranchLocations());
        return Double.compare(distA, distB);
    }

    /**
     * Gets the earliest expiration date from a merchant's deals.
     */
    private Long getEarliestExpiration(MerchantDealsWithLocationResponseDto dealsWithLocation) {
        return dealsWithLocation.getDeals().stream()
                .map(NotificationDealResponseDto::getExpiredOn)
                .filter(Objects::nonNull)
                .min(Long::compare)
                .orElse(Long.MAX_VALUE);
    }

    /**
     * Result of pagination operation.
     */
    private static class PaginationResult {
        final List<?> pagedResult;
        final long totalElements;
        final int totalPages;

        PaginationResult(List<?> pagedResult, long totalElements, int totalPages) {
            this.pagedResult = pagedResult;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
        }
    }

    /**
     * Applies pagination to the results.
     */
    private <T> PaginationResult applyPagination(List<T> allItems, int pageNumber, int pageSize) {
        long totalElements = allItems.size();
        int totalPages = totalElements > 0 ? (int) Math.ceil(totalElements / (double) pageSize) : 0;

        List<T> pagedResult;
        if (totalElements == 0 || pageNumber >= totalPages) {
            pagedResult = new ArrayList<>();
        } else {
            int fromIndex = pageNumber * pageSize;
            int toIndex = (int) Math.min(fromIndex + pageSize, totalElements);
            pagedResult = new ArrayList<>(allItems.subList(fromIndex, toIndex));
        }

        return new PaginationResult(pagedResult, totalElements, totalPages);
    }

    /**
     * Builds the response DTO.
     */
    @SuppressWarnings("unchecked")
    private MerchantDealsWithLocationListResponseDto buildDealsResponse(
            PaginationResult paginationResult, FilterParams params,
            DealFilterType filterType, double latitude, double longitude) {
        
        List<MerchantDealsWithLocationResponseDto> pagedResult = 
                (List<MerchantDealsWithLocationResponseDto>) paginationResult.pagedResult;
        
        MerchantDealsWithLocationListResponseDto response = new MerchantDealsWithLocationListResponseDto();
        response.setDeals(pagedResult);
        response.setSearchRadiusKm(params.searchRadius);
        response.setPage(params.pageNumber);
        response.setSize(params.pageSize);
        response.setTotalElements(paginationResult.totalElements);
        response.setTotalPages(paginationResult.totalPages);

        log.info("Returning {} deals with locations for lat: {}, lon: {} (radius: {} km, filterType: {}, page: {}, size: {}, totalElements: {}, totalPages: {})",
                pagedResult.size(), latitude, longitude, params.searchRadius, filterType, 
                params.pageNumber, params.pageSize, paginationResult.totalElements, paginationResult.totalPages);

        return response;
    }

    /**
     * Filters and builds deals with locations based on search term.
     * Filter type filtering is already done at the endpoint level.
     */
    private List<MerchantDealsWithLocationResponseDto> filterAndBuildDealsWithLocations(
            BulkMerchantDealsResponseDto bulkDealsByMerchantIds,
            Map<String, List<Branch>> branchesByMerchantId,
            double latitude,
            double longitude,
            String searchTerm) {

        List<MerchantDealsWithLocationResponseDto> results = new ArrayList<>();

        for (MerchantDealsResponseDto dealGroup : bulkDealsByMerchantIds.getDealGroups()) {
            MerchantDealsWithLocationResponseDto dealsWithLocation = 
                    buildDealsWithLocation(dealGroup, branchesByMerchantId, latitude, longitude, searchTerm);
            
            if (dealsWithLocation != null) {
                results.add(dealsWithLocation);
            }
        }

        return results;
    }

    /**
     * Builds a MerchantDealsWithLocationResponseDto for a deal group if it has valid branches and deals.
     */
    private MerchantDealsWithLocationResponseDto buildDealsWithLocation(
            MerchantDealsResponseDto dealGroup,
            Map<String, List<Branch>> branchesByMerchantId,
            double latitude,
            double longitude,
            String searchTerm) {
        
        String merchantId = dealGroup.getMerchant().getId();
        List<Branch> merchantBranches = branchesByMerchantId.getOrDefault(merchantId, new ArrayList<>());

        if (merchantBranches.isEmpty()) {
            return null;
        }

        List<LocationResponseDto> branchLocations = convertBranchesToLocations(merchantBranches, latitude, longitude);
        List<NotificationDealResponseDto> filteredDeals = filterDealsBySearchTerm(dealGroup.getDeals(), searchTerm);

        if (filteredDeals.isEmpty()) {
            return null;
        }

        MerchantDealsWithLocationResponseDto dealsWithLocation = new MerchantDealsWithLocationResponseDto();
        dealsWithLocation.setMerchant(dealGroup.getMerchant());
        dealsWithLocation.setDeals(filteredDeals);
        dealsWithLocation.setBranchLocations(branchLocations);

        return dealsWithLocation;
    }

    /**
     * Converts branches to LocationResponseDto list sorted by distance.
     */
    private List<LocationResponseDto> convertBranchesToLocations(
            List<Branch> branches, double latitude, double longitude) {
        
        return branches.stream()
                .map(branch -> new LocationResponseDto(branch, latitude, longitude))
                .sorted(Comparator.comparing(
                        loc -> loc.getDistanceKm() != null ? loc.getDistanceKm() : Double.MAX_VALUE))
                .collect(Collectors.toList());
    }

    /**
     * Filters deals by search term if provided.
     */
    private List<NotificationDealResponseDto> filterDealsBySearchTerm(
            List<NotificationDealResponseDto> deals, String searchTerm) {
        
        if (deals == null) {
            return new ArrayList<>();
        }

        if (searchTerm == null) {
            return new ArrayList<>(deals);
        }

        String normalizedSearchTerm = searchTerm.toLowerCase();
        return deals.stream()
                .filter(deal -> matchesSearchTerm(deal, normalizedSearchTerm))
                .collect(Collectors.toList());
    }

    /**
     * Checks if a deal matches the search term.
     */
    private boolean matchesSearchTerm(NotificationDealResponseDto deal, String normalizedSearchTerm) {
        String dealTitle = deal.getTitle() != null ? deal.getTitle().toLowerCase() : "";
        String dealSubTitle = deal.getSubTitle() != null ? deal.getSubTitle().toLowerCase() : "";
        return dealTitle.contains(normalizedSearchTerm) || dealSubTitle.contains(normalizedSearchTerm);
    }
}
