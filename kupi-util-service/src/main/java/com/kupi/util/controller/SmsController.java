package com.kupi.util.controller;

import com.kupi.util.configuration.Translator;
import com.kupi.util.domain.request.SmsRequestDto;
import com.kupi.util.enums.ErrorResponseStatusType;
import com.kupi.util.enums.SuccessResponseStatusType;
import com.kupi.util.exception.UtilServiceException;
import com.kupi.util.service.sms.SmsGateway;
import com.kupi.util.service.sms.SmsService;
import com.kupi.util.util.Validator;
import com.kupi.util.wrapper.ResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * SMS controller
 */
@Slf4j
@RestController
@Validated
@RequestMapping("api/v1/sms")
public class SmsController extends Controller {

    private final Validator validator;
    private final Logger logger = LoggerFactory.getLogger(SmsController.class);
    private final SmsService smsService;

    public SmsController(Translator translator, Validator validator, SmsService smsService) {
        super(translator);
        this.validator = validator;
        this.smsService = smsService;
    }

    /**
     * SMS send
     *
     * @param timeZone      timeZone
     * @param smsRequestDto smsRequestDto
     * @return ResponseEntity
     */
    @PostMapping("/send")
    public ResponseEntity<ResponseWrapper> sendSms(
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @RequestBody SmsRequestDto smsRequestDto
    ) {
        try {
            if (!isValidTimeZone(timeZone)) {
                logger.debug("Invalid time zone, Time zone: {}", timeZone);
                return getErrorResponse(ErrorResponseStatusType.INVALID_TIMEZONE);
            }
            if (!smsRequestDto.isRequiredAvailable()) {
                return getErrorResponse(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
            if (!validator.isValidMobileNo(smsRequestDto.getRecipientNo().getNo())) {
                return getErrorResponse(ErrorResponseStatusType.INVALID_MOBILE_NO);
            }
            var response = smsService.sendSms(smsRequestDto);
            String objectToJson = smsRequestDto.getRecipientNo().toLogJson();
            logger.info("Successfully sent the SMS to: {}, response status: {}, response request: {}", 
                    objectToJson, response.getStatus(), response.getRequest());
            logger.debug("Full response: {}", response);
            return getSuccessResponse(null, SuccessResponseStatusType.SMS_SUCCESS_MESSAGE);
        } catch (UtilServiceException e) {
            logger.error("Sending sms was failed to: {}", smsRequestDto.getRecipientNo(), e);
            return getInternalServerError();
        }
    }
}
