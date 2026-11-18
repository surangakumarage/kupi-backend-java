package com.kupi.cc.auth.controller;

import com.kupi.cc.auth.configuration.Translator;
import com.kupi.cc.auth.domain.response.PreferredMerchantResponseDto;
import com.kupi.cc.auth.enums.ErrorResponseStatusType;
import com.kupi.cc.auth.enums.SuccessResponseStatusType;
import com.kupi.cc.auth.exception.AuthServiceException;
import com.kupi.cc.auth.service.PreferredMerchantService;
import com.kupi.cc.auth.wrapper.ResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing user's preferred merchants
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/preferred-merchants")
public class PreferredMerchantController extends Controller {

    private final PreferredMerchantService preferredMerchantService;
    Translator translator;

    public PreferredMerchantController(PreferredMerchantService preferredMerchantService, Translator translator) {
        super(translator);
        this.preferredMerchantService = preferredMerchantService;
        this.translator = translator;
    }

    /**
     * This method is used to update preferred merchants for a user.
     * It replaces the entire list of preferred merchants.
     *
     * @param userId      user ID (from header)
     * @param merchantIds list of merchant IDs to set as preferred
     * @return success/failure response
     */
    @PutMapping(consumes = APPLICATION_JSON_UTF_8, produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> updatePreferredMerchants(
            @RequestHeader(name = HEADER_USER_ID) String userId,
            @RequestBody List<String> merchantIds) {
        try {
            preferredMerchantService.updatePreferredMerchants(userId, merchantIds);
            log.debug("Successfully updated preferred merchants for userId: {} with list: {}", userId, merchantIds);

            return getSuccessResponse(SuccessResponseStatusType.UPDATED_PREFERRED_MERCHANTS, null);

        } catch (AuthServiceException e) {
            log.error("Failed to update preferred merchants for userId: {}", userId, e);
            return getInternalServerError();
        }
    }
    @GetMapping(produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> getPreferredMerchants(
            @RequestHeader(name = HEADER_USER_ID) String userId) {
        try {
            PreferredMerchantResponseDto preferredMerchantResponseDto =
                    preferredMerchantService.getPreferredMerchantIds(userId);
            log.debug("Fetched preferred merchant list for userId: {}", userId);
            return getSuccessResponse(SuccessResponseStatusType.GET_PREFERRED_MERCHANTS, preferredMerchantResponseDto);

        } catch (AuthServiceException e) {
            log.error("Failed to fetch preferred merchants for userId: {}", userId, e);
            return getInternalServerError();
        }
    }

    /**
     * This method is used to get all preferred merchants for a user.
     *
     * @param userId user ID (from header)
     * @return list of preferred merchant IDs
     */
    @GetMapping(path = "/internal", produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> getPreferredMerchantsForInternal(
            @RequestHeader(name = HEADER_USER_ID) String userId) {
        try {
            PreferredMerchantResponseDto preferredMerchantResponseDto =
                    preferredMerchantService.getPreferredMerchantIds(userId);
            log.debug("Internal API Fetched preferred merchant list for userId: {}", userId);
            return getSuccessResponse(SuccessResponseStatusType.GET_PREFERRED_MERCHANTS, preferredMerchantResponseDto);

        } catch (AuthServiceException e) {
            log.error("Failed to fetch preferred merchants for userId: {}", userId, e);
            return getInternalServerError();
        }
    }
}

