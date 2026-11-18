package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.request.BulkUserRequestDto;
import com.kupi.cc.base.domain.response.BasicBusinessResponseDto;
import com.kupi.cc.base.domain.response.BulkMerchantBusinessResponseDto;
import com.kupi.cc.base.exception.KupiOrderPaymentException;
import com.kupi.cc.base.wrapper.BulkMerchantResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class AuthService {
    protected static final String APP_KEY = "app-key";
    private static final String TIME_ZONE_HEADER = "Time-Zone";
    private static final String FAILED_GET_USER_BY_ID = "Getting user by id was failed from auth service";
    private static final String FAILED_LOG_MESSAGE = "{}. statusCode: {}, body: {}";
    private final String getAppKey;
    private final String getBulkMerchantsInfo;
    @Autowired
    private RestTemplate restTemplate;

    public AuthService(@Value("${auth.baseUrl}") String baseUrl,
                       @Value("${auth.uri.getBulkMerchantInfo}") String getBulkMerchantInfo,
                       @Value("${auth.appKey}") String getAppKey,
                       RestTemplate restTemplate) {
        this.getBulkMerchantsInfo = baseUrl + getBulkMerchantInfo;
        this.restTemplate = restTemplate;
        this.getAppKey = getAppKey;
    }

    /**
     * This method returns header for getBulkMerchant url.
     *
     * @return headers
     */
    private HttpHeaders getAuthHeadersForBulkMerchant(String timeZone) {
        var headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(TIME_ZONE_HEADER, timeZone);
        headers.set(APP_KEY, getAppKey);
        return headers;
    }

    /**
     * This method returns merchant business details for blog
     *
     * @param userIds userIdsgetBulkMerchantBusinessResponse
     * @return BulkMerchantBusinessResponseDto
     */
    private BulkMerchantBusinessResponseDto getBulkMerchantBusinessResponse(BulkUserRequestDto userIds, String timeZone) {
        HttpHeaders headers = getAuthHeadersForBulkMerchant(timeZone);
        HttpEntity<BulkUserRequestDto> entity = new HttpEntity<>(userIds, headers);
        try {
            log.debug("Calling auth service to get merchant business bulk by Id. url: {}.", getBulkMerchantsInfo);
            ResponseEntity<BulkMerchantResponseWrapper> result =
                    restTemplate.exchange(getBulkMerchantsInfo, HttpMethod.POST, entity, BulkMerchantResponseWrapper.class);
            String responseBody = result.getBody() == null ? "" : Objects.requireNonNull(result.getBody()).toLogJson();
            log.debug("Getting merchant business bulk by id was successful. statusCode: {}, response: {}",
                    result.getStatusCode(), responseBody);
            return Objects.requireNonNull(result.getBody()).getData();
        } catch (HttpClientErrorException e) {
            log.error(FAILED_LOG_MESSAGE, FAILED_GET_USER_BY_ID, e.getRawStatusCode(), e.getResponseBodyAsString());
            throw new KupiOrderPaymentException(FAILED_GET_USER_BY_ID, e);
        }
    }

    /**
     * This method returns a Merchant/ Bank Map by user id and bulk request dto
     *
     * @param userIds    BulkUser Id List
     * @param timeZone timeZone
     * @return Merchant map
     */
    public Map<String, BasicBusinessResponseDto> getMerchantMap(BulkUserRequestDto userIds, String timeZone) {
        var bulkUserResponse = getBulkMerchantBusinessResponse(userIds, timeZone);
        Map<String, BasicBusinessResponseDto> basicBusinessResponseDtoHashMap = new HashMap<>();
        for (BasicBusinessResponseDto basicBusinessResponseDto : bulkUserResponse.getMerchants()) {
            basicBusinessResponseDtoHashMap.put(basicBusinessResponseDto.getId(), basicBusinessResponseDto);
        }
        return basicBusinessResponseDtoHashMap;
    }
}