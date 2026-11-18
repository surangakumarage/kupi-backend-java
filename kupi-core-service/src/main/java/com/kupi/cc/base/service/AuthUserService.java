package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.AuthUser;
import com.kupi.cc.base.domain.entity.Deal;
import com.kupi.cc.base.domain.request.BulkUserRequestDto;
import com.kupi.cc.base.domain.response.*;
import com.kupi.cc.base.domain.response.dashboard.AuthSummaryResponseDto;
import com.kupi.cc.base.enums.ApprovalStatus;
import com.kupi.cc.base.enums.UserType;
import com.kupi.cc.base.exception.InvalidUserException;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.wrapper.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Auth User Service
 */
@Slf4j
@Service
public class AuthUserService {
    protected static final String TIME_ZONE_HEADER = "Time-Zone";
    protected static final String APP_KEY = "app-key";
    protected static final String TIME_ZONE_VALUE = "Asia/Colombo";
    private static final String USER_ID_HEADER = "User-Id";
    private static final String FAILED_GET_USER_BY_ID = "Getting user by id was failed from auth service";
    private static final String FAILED_GET_USER_DATA = "Requesting user data from auth service was failed.";
    private static final String FAILED_GET_MERCHANT_KEY = "Getting merchant access key from auth service was failed.";
    private static final String FAILED_LOG_MESSAGE = "{}. statusCode: {}, body: {}";
    private static final String INVALID_USER_ID = "Invalid userId: ";
    private static final String INVALID_USER_TYPE = "Invalid userType: ";
    private static final String FAILED_TO_GET_BUSINESS_INFO = "Failed to get approved business info from auth service. userId: ";
    private static final String FAILED_TO_GET_TODAY_SUMMARY = "Failed to get today's summary from auth service. ";
    private static final String FAILED_GET_ALL_MERCHANTS = "Failed to get all merchants from auth service";
    private static final int BULK_MERCHANT_LIMIT = 25;
    private static final String MERCHANT_ID_REPLACE_PHASE = "##MERCHANT-ID##";
    private static final String BULK_INFO_REPLACE_PHASE = "##TO-USER-TYPE##";
    private static final String BANK_ID_REPLACE_PHASE = "##BANK-ID##";
    private static final String USER_TYPE_REPLACE_PHASE = "##TO-USER-TYPE##";
    private static final String OR = " or ";
    private static final String PAGE_REPLACE_PHASE = "##PAGE##";
    private static final String SIZE_REPLACE_PHASE = "##SIZE##";
    private static final String SEARCH_REPLACE_PHASE = "##SEARCH##";
    private final String getUserUrl;
    private final RestTemplate restTemplate;
    private final String getBulkMerchantsInfo;
    private final String getBulkUsersInfo;
    private final String getMerchantInfoUrl;
    private final String getBankInfoUrl;
    private final String getTodaySummaryUrl;
    private final String getMerchantAccessKeyUrl;
    private final String getAppKey;
    private final String getAllMerchantsUrl;
    private final String getSliderImagesUrl;
    private final String getBulkSliderImagesUrl;

    @Autowired
    public AuthUserService(@Value("${auth.baseUrl}") String baseUrl,
                           @Value("${auth.uri.getUser}") String getUserUri,
                           @Value("${auth.uri.getBulkMerchantInfo}") String getBulkMerchantInfo,
                           @Value("${auth.uri.getBulkUserInfo}") String getBulkUserInfo,
                           @Value("${auth.uri.getMerchantInfo}") String getMerchantInfo,
                           @Value("${auth.uri.getBankInfo}") String getBankInfo,
                           @Value("${auth.uri.getTodaySummary}") String getTodaySummaryUrl,
                           @Value("${auth.uri.getMerchantApiAccessKey}") String getMerchantAccessKeyUrl,
                           @Value("${auth.appKey}") String getAppKey,
                           @Value("${auth.uri.getAllMerchants}") String getAllMerchants,
                           @Value("${auth.uri.getSliderImages}") String getSliderImages,
                           @Value("${auth.uri.getBulkSliderImages}") String getBulkSliderImages,
                           RestTemplate restTemplate) {
        this.getMerchantAccessKeyUrl = baseUrl + getMerchantAccessKeyUrl;
        this.getUserUrl = baseUrl + getUserUri;
        this.getBulkMerchantsInfo = baseUrl + getBulkMerchantInfo;
        this.getBulkUsersInfo = baseUrl + getBulkUserInfo;
        this.getMerchantInfoUrl = baseUrl + getMerchantInfo;
        this.getBankInfoUrl = baseUrl + getBankInfo;
        this.getTodaySummaryUrl = baseUrl + getTodaySummaryUrl;
        this.getAllMerchantsUrl = baseUrl + getAllMerchants;
        this.getSliderImagesUrl = baseUrl + getSliderImages;
        this.getBulkSliderImagesUrl = baseUrl + getBulkSliderImages;
        this.restTemplate = restTemplate;
        this.getAppKey = getAppKey;
    }

    /**
     * This method gets slider images for a merchant from auth service (internal endpoint).
     *
     * @param merchantId merchant id
     * @return MerchantSliderImageListResponseDto
     */
    public MerchantSliderImageListResponseDto getSliderImagesForMerchant(String merchantId) {
        HttpHeaders headers = getAuthHeadersForBulkMerchant();
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        try {
            String url = getSliderImagesUrl.replace(MERCHANT_ID_REPLACE_PHASE, merchantId);
            log.debug("Calling auth service to get slider images by merchant id. url: {}, merchantId: {}",
                    url, merchantId);
            ResponseEntity<MerchantSliderImageListResponseWrapper> result =
                    restTemplate.exchange(url, HttpMethod.GET, entity, MerchantSliderImageListResponseWrapper.class);
            String responseBody = result.getBody() == null ? "" : Objects.requireNonNull(result.getBody()).toLogJson();
            log.debug("Getting slider images by merchant id was successful. statusCode: {}, response: {}",
                    result.getStatusCode(), responseBody);
            return Objects.requireNonNull(result.getBody()).getData();
        } catch (HttpClientErrorException e) {
            log.error(FAILED_LOG_MESSAGE, "Failed to get slider images from auth service",
                    e.getRawStatusCode(), e.getResponseBodyAsString());
            throw new KupiCoreException("Failed to get slider images from auth service", e);
        }
    }

    /**
     * This method gets slider images for multiple merchants from auth service (internal bulk endpoint).
     *
     * @param merchantIds list of merchant ids
     * @return BulkMerchantSliderImageListResponseDto
     */
    public BulkMerchantSliderImageListResponseDto getSliderImagesForMerchants(List<String> merchantIds) {
        HttpHeaders headers = getAuthHeadersForBulkMerchant();
        HttpEntity<BulkUserRequestDto> entity = new HttpEntity<>(new BulkUserRequestDto(merchantIds), headers);
        try {
            log.debug("Calling auth service to get bulk slider images for {} merchants", 
                    merchantIds != null ? merchantIds.size() : 0);
            ResponseEntity<BulkMerchantSliderImageListResponseWrapper> result =
                    restTemplate.exchange(getBulkSliderImagesUrl, HttpMethod.POST, entity,
                            BulkMerchantSliderImageListResponseWrapper.class);
            String responseBody = result.getBody() == null ? "" : Objects.requireNonNull(result.getBody()).toLogJson();
            log.debug("Getting bulk slider images was successful. statusCode: {}, response: {}",
                    result.getStatusCode(), responseBody);
            return Objects.requireNonNull(result.getBody()).getData();
        } catch (HttpClientErrorException e) {
            log.error(FAILED_LOG_MESSAGE, "Failed to get bulk slider images from auth service",
                    e.getRawStatusCode(), e.getResponseBodyAsString());
            throw new KupiCoreException("Failed to get bulk slider images from auth service", e);
        }
    }

    /**
     * This method get merchant business info by merchant id.
     *
     * @param token      token
     * @param merchantId merchant id
     * @return Merchant Business info
     */
    public BusinessMerchantResponseDto getMerchantBusinessByMerchantId(String token, String merchantId) {
        HttpHeaders headers = getAuthHeaders(token, merchantId);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        try {
            var url = getMerchantInfoUrl.replace(MERCHANT_ID_REPLACE_PHASE, merchantId);
            log.debug("Calling auth service to get the merchant business info by merchant id. url: {}, merchantId: {}",
                    url, merchantId);
            var result =
                    restTemplate.exchange(url, HttpMethod.GET, entity, MerchantBusinessResponseWrapper.class);
            String responseBody = Objects.requireNonNull(result.getBody()).getData().toLogJson();
            log.debug("Getting merchant business info by id was successful. statusCode: {}, response: {}",
                    result.getStatusCode(), responseBody);
            return Objects.requireNonNull(result.getBody()).getData();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().equals(HttpStatus.BAD_REQUEST)) {
                throw new InvalidUserException(FAILED_TO_GET_BUSINESS_INFO + merchantId, e);
            }
            throw new KupiCoreException(FAILED_TO_GET_BUSINESS_INFO + merchantId, e);
        }
    }

    /**
     * This method get bank business info by bank id.
     *
     * @param token  token
     * @param bankId bankId
     * @return Bank business info.
     */
    public BusinessMerchantResponseDto getBankBusinessByBankId(String token, String bankId) {
        HttpHeaders headers = getAuthHeaders(token, bankId);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        try {
            var url = getBankInfoUrl.replace(BANK_ID_REPLACE_PHASE, bankId);
            log.debug("Calling auth service to get the bank business info by bank id. url: {}, bankId: {}",
                    url, bankId);
            var result =
                    restTemplate.exchange(url, HttpMethod.GET, entity, MerchantBusinessResponseWrapper.class);
            String responseBody = Objects.requireNonNull(result.getBody()).getData().toLogJson();
            log.debug("Getting bank business info by id was successful. statusCode: {}, response: {}",
                    result.getStatusCode(), responseBody);
            return Objects.requireNonNull(result.getBody()).getData();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().equals(HttpStatus.BAD_REQUEST)) {
                throw new InvalidUserException(FAILED_TO_GET_BUSINESS_INFO + bankId, e);
            }
            throw new KupiCoreException(FAILED_TO_GET_BUSINESS_INFO + bankId, e);
        }
    }

    /**
     * This method get auth user by id.
     *
     * @param userId userId
     * @param token  token
     * @return auth user
     */
    public AuthUser getUserByUserId(String userId, String token) {
        HttpHeaders headers = getAuthHeaders(token, userId);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        try {
            log.debug("Calling auth service to get the user by Id. url: {}, userId: {}", getUserUrl, userId);
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
            throw new KupiCoreException(FAILED_GET_USER_DATA, e);
        }
    }

    /**
     * This method returns merchant business details for blog
     *
     * @param userIds userIdsgetBulkMerchantBusinessResponse
     * @return BulkMerchantBusinessResponseDto
     */
    private BulkMerchantBusinessResponseDto getBulkMerchantBusinessResponse(BulkUserRequestDto userIds, UserType toUserType) {
        HttpHeaders headers = getAuthHeadersForBulkMerchant();
        HttpEntity<BulkUserRequestDto> entity = new HttpEntity<>(userIds, headers);
        String getBulkInfoUrl = (UserType.MERCHANT.equals(toUserType)) ? getBulkMerchantsInfo
                .replace(BULK_INFO_REPLACE_PHASE, UserType.MERCHANT.toString()) :
                getBulkMerchantsInfo.replace(BULK_INFO_REPLACE_PHASE, UserType.BANK.toString());
        try {
            log.debug("Calling auth service to get merchant business bulk by Id. url: {}.",
                    getBulkInfoUrl);
            ResponseEntity<BulkMerchantResponseWrapper> result =
                    restTemplate.exchange(getBulkInfoUrl, HttpMethod.POST, entity, BulkMerchantResponseWrapper.class);
            String responseBody = result.getBody() == null ? "" : Objects.requireNonNull(result.getBody()).toLogJson();
            log.debug("Getting merchant business bulk by id was successful. statusCode: {}, response: {}",
                    result.getStatusCode(), responseBody);
            return Objects.requireNonNull(result.getBody()).getData();
        } catch (HttpClientErrorException e) {
            log.error(FAILED_LOG_MESSAGE, FAILED_GET_USER_BY_ID, e.getRawStatusCode(), e.getResponseBodyAsString());
            throw new KupiCoreException(FAILED_GET_USER_BY_ID, e);
        }
    }

    /**
     * This method is used to get today's summary from auth service.
     *
     * @param userId userId
     * @param token  token
     * @return today summary response.
     */
    public AuthSummaryResponseDto getTodaySummaryResponse(String userId, String token, UserType userType) {
        HttpHeaders headers = getAuthHeaders(token, userId);
        HttpEntity<BulkUserRequestDto> entity = new HttpEntity<>(null, headers);
        try {
            var url = getTodaySummaryUrl.replace(USER_TYPE_REPLACE_PHASE, userType.getType());
            log.debug("Calling auth service to get today summary. Url: {}, UserId: {}", getTodaySummaryUrl, userId);
            ResponseEntity<AuthSummaryResponseWrapper> result =
                    restTemplate.exchange(url, HttpMethod.GET, entity, AuthSummaryResponseWrapper.class);
            String responseBody = result.getBody() == null ? "" : Objects.requireNonNull(result.getBody()).toLogJson();
            log.debug("Getting today summary was successful. statusCode: {}, response: {}",
                    result.getStatusCode(), responseBody);
            return Objects.requireNonNull(result.getBody()).getData();
        } catch (HttpClientErrorException e) {
            log.error(FAILED_LOG_MESSAGE, FAILED_TO_GET_TODAY_SUMMARY, e.getRawStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode().value() == HttpStatus.BAD_REQUEST.value()) {
                throw new InvalidUserException(INVALID_USER_ID + userId + OR + INVALID_USER_TYPE + userType);
            }
            throw new KupiCoreException(FAILED_TO_GET_TODAY_SUMMARY, e);
        }
    }

    /**
     * This method returns a Merchant/ Bank Map by user id and bulk request dto
     *
     * @param userIds    BulkUser Id List
     * @param toUserType toUserType
     * @return Merchant map
     */
    public Map<String, MerchantBusinessResponseDto> getMerchantMap(
            BulkUserRequestDto userIds, UserType toUserType) {
        var bulkUserResponse = getBulkMerchantBusinessResponse(userIds, toUserType);
        Map<String, MerchantBusinessResponseDto> merchantResponseDtoHashMap = new HashMap<>();
        for (MerchantBusinessResponseDto merchantBusinessResponseDto : bulkUserResponse.getMerchants()) {
            merchantResponseDtoHashMap.put(merchantBusinessResponseDto.getId(), merchantBusinessResponseDto);
        }
        return merchantResponseDtoHashMap;
    }

    /**
     * Returns merchant/bank IDs that are approved and active (not blocked) according to auth service.
     * Used to filter deal listings so blocked merchants do not appear in mobile app.
     * Batches requests by BULK_MERCHANT_LIMIT to respect auth bulk endpoint limits.
     *
     * @param merchantIds list of merchant/bank IDs to check
     * @param userType    MERCHANT or BANK
     * @return set of IDs that are APPROVED and active (not blocked)
     */
    public Set<String> getEligibleMerchantIds(List<String> merchantIds, UserType userType) {
        if (merchantIds == null || merchantIds.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> eligible = new HashSet<>();
        for (int i = 0; i < merchantIds.size(); i += BULK_MERCHANT_LIMIT) {
            int end = Math.min(i + BULK_MERCHANT_LIMIT, merchantIds.size());
            List<String> batch = merchantIds.subList(i, end);
            BulkUserRequestDto bulkRequest = new BulkUserRequestDto(batch);
            try {
                Map<String, MerchantBusinessResponseDto> merchantMap = getMerchantMap(bulkRequest, userType);
                for (Map.Entry<String, MerchantBusinessResponseDto> e : merchantMap.entrySet()) {
                    MerchantBusinessResponseDto m = e.getValue();
                    if (m != null && m.getApprovalStatus() == ApprovalStatus.APPROVED && m.isActive()) {
                        eligible.add(e.getKey());
                    }
                }
            } catch (KupiCoreException e) {
                log.warn("Auth service call failed for merchant batch, skipping batch: {}", e.getMessage());
            }
        }
        return eligible;
    }

    /**
     * This method returns a Merchant Map for deal page.
     *
     * @param dealPage deal page
     * @return Merchant map
     */
    public Map<String, MerchantBusinessResponseDto> getMerchantMapForDeals(Page<Deal> dealPage,
                                                                           UserType userType) {
        List<String> userIds = new ArrayList<>();
        if (!dealPage.getContent().isEmpty()) {
            dealPage.getContent().forEach(deal -> userIds.add(deal.getMerchantId()));
            BulkUserRequestDto bulkUserRequestDto = new BulkUserRequestDto(userIds);
            return getMerchantMap(bulkUserRequestDto, userType);
        }
        return new HashMap<>();
    }

    /**
     * This method returns headers for auth service urls.
     *
     * @param token token
     * @return headers
     */
    private HttpHeaders getAuthHeaders(String token, String userId) {
        if (token == null || token.trim().isEmpty()) {
            log.error("Auth token is null or empty when building auth headers for userId: {}", userId);
            throw new KupiCoreException("Auth token is missing or invalid");
        }
        var headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token.trim());
        headers.set(TIME_ZONE_HEADER, TIME_ZONE_VALUE);
        headers.set(USER_ID_HEADER, userId);
        return headers;
    }

    /**
     * This method returns header for getBulkMerchant url.
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

    /**
     * This method returns a Users Map by user id and bulk request dto
     *
     * @param userId  user id
     * @param userIds BulkUser Id List
     * @return Merchant map
     */
    public Map<String, BasicUserResponseDto> getUserMap(String userId, BulkUserRequestDto userIds) {
        var bulkUserResponse = getBulkUserResponse(userId, userIds);
        Map<String, BasicUserResponseDto> userResponseDtoHashMap = new HashMap<>();
        for (BasicMerchantBusinessResponseDto basicUserResponseDto : bulkUserResponse.getUsers()) {
            userResponseDtoHashMap.put(basicUserResponseDto.getId(),
                    new BasicUserResponseDto(basicUserResponseDto.getId(), basicUserResponseDto.getName(),
                            basicUserResponseDto.getImageUrl(), basicUserResponseDto.getMobileNo()));
        }
        return userResponseDtoHashMap;
    }

    /**
     * This method returns user details
     *
     * @param userId  userId
     * @param userIds userIds
     * @return BulkUserResponseDto
     */
    public BulkUserResponseDto getBulkUserResponse(String userId, BulkUserRequestDto userIds) {
        HttpHeaders headers = getAuthHeadersForBulkMerchant();
        HttpEntity<BulkUserRequestDto> entity = new HttpEntity<>(userIds, headers);
        try {
            log.debug("Calling auth service to get user bulk by Id. url: {}, userId: {}",
                    getBulkUsersInfo, userId);
            ResponseEntity<BulkUserResponseWrapper> result =
                    restTemplate.exchange(getBulkUsersInfo, HttpMethod.POST, entity, BulkUserResponseWrapper.class);
            String responseBody = result.getBody() == null ? "" : Objects.requireNonNull(result.getBody()).toLogJson();
            log.debug("Getting user bulk by id was successful. statusCode: {}, response: {}",
                    result.getStatusCode(), responseBody);
            return Objects.requireNonNull(result.getBody()).getData();
        } catch (HttpClientErrorException e) {
            log.error(FAILED_LOG_MESSAGE, FAILED_GET_USER_BY_ID, e.getRawStatusCode(), e.getResponseBodyAsString());
            throw new KupiCoreException(FAILED_GET_USER_BY_ID, e);
        }
    }

    /**
     * This method is used to get api access key for merchant from auth service.
     *
     * @param merchantId merchantId
     * @return keyResponseDto
     */
    public KeyResponseDto getApiAccessKeyForMerchant(String merchantId) {
        HttpHeaders headers = getAuthHeadersForBulkMerchant();
        HttpEntity<BulkUserRequestDto> entity = new HttpEntity<>(headers);
        try {
            String url = getMerchantAccessKeyUrl.replace(MERCHANT_ID_REPLACE_PHASE, merchantId);
            log.debug("Calling auth service to get merchant api access key. url: {}, merchantId: {}", url, merchantId);
            ResponseEntity<AccessKeyResponseWrapper> result =
                    restTemplate.exchange(url, HttpMethod.GET, entity, AccessKeyResponseWrapper.class);
            String responseBody = result.getBody() == null ? "" : Objects.requireNonNull(result.getBody()).toLogJson();
            log.debug("Getting merchant api access key was successful. statusCode: {}, response: {}",
                    result.getStatusCode(), responseBody);
            return Objects.requireNonNull(result.getBody()).getData();
        } catch (HttpClientErrorException e) {
            log.error(FAILED_LOG_MESSAGE, FAILED_GET_MERCHANT_KEY, e.getRawStatusCode(), e.getResponseBodyAsString());
            throw new KupiCoreException(FAILED_GET_MERCHANT_KEY, e);
        }
    }

    /**
     * This method gets all merchants with pagination and search from auth service.
     *
     * @param userId     userId
     * @param page       page number
     * @param size       page size
     * @param searchTerm search term
     * @return AllMerchantListResponseDto
     */
    public AllMerchantListResponseDto getAllMerchants( String userId, Integer page, Integer size, String searchTerm) {
        HttpHeaders headers = getAuthHeadersForBulkMerchant();
        headers.set("User-Id", userId);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        try {
            String url = getAllMerchantsUrl
                    .replace(PAGE_REPLACE_PHASE, String.valueOf(page))
                    .replace(SIZE_REPLACE_PHASE, String.valueOf(size))
                    .replace(SEARCH_REPLACE_PHASE, !searchTerm.isEmpty() ? searchTerm : "ALL");
            log.debug("Calling auth service to get all merchants. url: {}, page: {}, size: {}, searchTerm: {}",
                    url, page, size, searchTerm);
            ResponseEntity<AllMerchantListResponseWrapper> result =
                    restTemplate.exchange(url, HttpMethod.GET, entity, AllMerchantListResponseWrapper.class);
            String responseBody = result.getBody() == null ? "" : Objects.requireNonNull(result.getBody()).toLogJson();
            log.debug("Getting all merchants was successful. statusCode: {}, response: {}",
                    result.getStatusCode(), responseBody);
            return Objects.requireNonNull(result.getBody()).getData();
        } catch (HttpClientErrorException e) {
            log.error(FAILED_GET_ALL_MERCHANTS, e);
            throw new KupiCoreException(FAILED_GET_ALL_MERCHANTS, e);
        }
    }
}