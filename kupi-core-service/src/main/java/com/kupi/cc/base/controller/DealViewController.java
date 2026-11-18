package com.kupi.cc.base.controller;

import com.kupi.cc.base.domain.request.DealViewRequestDto;
import com.kupi.cc.base.enums.ErrorResponseStatusType;
import com.kupi.cc.base.enums.SuccessResponseStatusType;
import com.kupi.cc.base.enums.UserType;
import com.kupi.cc.base.exception.InvalidDealException;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.service.DealViewService;
import com.kupi.cc.base.wrapper.ResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Deal View Controller
 * Handles tracking of deal detail page views
 */
@Slf4j
@RestController
@Validated
@RequestMapping("api/v1/deal-views")
public class DealViewController extends Controller {

    private final DealViewService dealViewService;

    @Autowired
    public DealViewController(DealViewService dealViewService, com.kupi.cc.base.configuration.Translator translator) {
        super(translator);
        this.dealViewService = dealViewService;
    }

    /**
     * Track a deal view when user views deal detail page
     *
     * @param userId             user id viewing the deal
     * @param timeZone           time zone
     * @param dealViewRequestDto deal view request dto containing dealId
     * @return SuccessResponse / ErrorResponse
     */
    @PostMapping("")
    public ResponseEntity<ResponseWrapper> trackDealView(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @RequestBody DealViewRequestDto dealViewRequestDto) {
        try {

            if (!dealViewRequestDto.isRequiredAvailable()) {
                return getErrorResponse(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
            dealViewService.trackDealView(userId, dealViewRequestDto.getDealId());
            log.debug("Successfully tracked deal view for userId: {}, dealId: {}",
                    userId, dealViewRequestDto.getDealId());
            return getSuccessResponse(null, SuccessResponseStatusType.TRACK_DEAL_VIEW);
        } catch (InvalidDealException e) {
            log.error("Invalid dealId: {} for tracking view. userId: {}, time zone: {}",
                    dealViewRequestDto.getDealId(), userId, timeZone, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_DEAL_ID);
        } catch (KupiCoreException e) {
            log.error("Tracking deal view was failed for userId: {}, dealId: {},",
                    userId, dealViewRequestDto.getDealId(), e);
            return getInternalServerError();
        }
    }
}

