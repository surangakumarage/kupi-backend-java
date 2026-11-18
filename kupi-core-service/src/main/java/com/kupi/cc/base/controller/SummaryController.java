package com.kupi.cc.base.controller;

import com.kupi.cc.base.configuration.Translator;
import com.kupi.cc.base.domain.response.dashboard.FullSummaryResponseDto;
import com.kupi.cc.base.enums.ErrorResponseStatusType;
import com.kupi.cc.base.enums.SuccessResponseStatusType;
import com.kupi.cc.base.enums.UserType;
import com.kupi.cc.base.exception.InvalidUserException;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.service.SummaryService;
import com.kupi.cc.base.wrapper.ResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Summary Controller
 */
@RestController
@Slf4j
@RequestMapping("/api/v1/summary")
public class SummaryController extends Controller {

    private final SummaryService summaryService;

    @Autowired
    public SummaryController(Translator translator, SummaryService summaryService) {
        super(translator);
        this.summaryService = summaryService;
    }

    /**
     * This method is used to get today's summary for admin & merchant.
     *
     * @param userId   userId
     * @param timeZone timeZone
     * @param userType userType
     * @param request  http request
     * @return today's summary response.
     */
    @GetMapping("/{userType}")
    public ResponseEntity<ResponseWrapper> getTodaySummary(@RequestHeader(name = USER_ID_HEADER) String userId,
                                                           @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                           @PathVariable UserType userType,
                                                           HttpServletRequest request) {
        try {
            String authToken = request.getHeader(AUTH_TOKEN_HEADER);
            FullSummaryResponseDto fullSummaryResponseDto =
                    summaryService.getFullSummary(timeZone, userType, userId, authToken);
            log.debug("Successfully returned Full summary for userId: {}.", userId);
            return getSuccessResponse(fullSummaryResponseDto, SuccessResponseStatusType.SUCCESSFULLY_RETURNED_SUMMARY);
        } catch (InvalidUserException e) {
            log.error("Invalid userId: {} or userType: {}", userId, userType, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_USER_ID_OR_TYPE);
        } catch (KupiCoreException e) {
            log.error("Returning today's summary failed for userId: {}, userType: {}", userId, userType, e);
            return getInternalServerError();
        }
    }
}
