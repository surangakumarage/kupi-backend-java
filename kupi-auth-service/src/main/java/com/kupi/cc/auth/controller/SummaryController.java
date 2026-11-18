package com.kupi.cc.auth.controller;

import com.kupi.cc.auth.configuration.Translator;
import com.kupi.cc.auth.domain.response.TodaySummaryResponseDto;
import com.kupi.cc.auth.enums.ErrorResponseStatusType;
import com.kupi.cc.auth.enums.SuccessResponseStatusType;
import com.kupi.cc.auth.exception.AuthServiceException;
import com.kupi.cc.auth.exception.InvalidUserException;
import com.kupi.cc.auth.service.UserService;
import com.kupi.cc.auth.wrapper.ResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Summary Controller
 */
@RestController
@Slf4j
@RequestMapping("/api/v1/summary")
public class SummaryController extends Controller {

    private final UserService userService;

    @Autowired
    public SummaryController(Translator translator, UserService userService) {
        super(translator);
        this.userService = userService;
    }

    /**
     * This method is used to get today's summary.
     *
     * @param userId   userId
     * @param timeZone timeZone
     * @param roleType roleType
     * @return today's summary response.
     */
    @GetMapping("/{roleType}")
    public ResponseEntity<ResponseWrapper> getTodaySummary(@RequestHeader(name = HEADER_USER_ID) String userId,
                                                           @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                           @PathVariable String roleType) {
        try {
            TodaySummaryResponseDto todaySummaryResponseDto = userService.getTodaySummary(timeZone, userId, roleType);
            return getSuccessResponse(SuccessResponseStatusType.GET_TODAY_SUMMARY, todaySummaryResponseDto);
        } catch (InvalidUserException e) {
            log.error("Returning today's summary failed for userId: {}, roleType: {}", userId, roleType, e);
            return getBadRequestError(ErrorResponseStatusType.INVALID_USER_ID_OR_ROLE);
        } catch (AuthServiceException e) {
            log.error("Returning today's summary failed for userId: {}, roleType: {}", userId, roleType, e);
            return getInternalServerError();
        }
    }
}
