package com.kupi.util.controller;

import com.kupi.util.configuration.Translator;
import com.kupi.util.domain.request.MobileNoRequestDto;
import com.kupi.util.domain.response.WhatsAppAvailabilityResponseDto;
import com.kupi.util.enums.ErrorResponseStatusType;
import com.kupi.util.enums.SuccessResponseStatusType;
import com.kupi.util.exception.UtilServiceException;
import com.kupi.util.service.whatsapp.WhatsAppApiClient;
import com.kupi.util.wrapper.ResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Validated
@RequestMapping("/api/v1/whatsapp")
public class WhatsAppController extends Controller {
    private final Logger logger = LoggerFactory.getLogger(WhatsAppController.class);

    private final WhatsAppApiClient whatsappApiClient;


    public WhatsAppController(Translator translator, WhatsAppApiClient whatsappApiClient) {
        super(translator);
        this.whatsappApiClient = whatsappApiClient;
    }

    @PostMapping("/check")
    public ResponseEntity<ResponseWrapper> checkWhatsAppAvailability(@RequestBody MobileNoRequestDto mobileNoRequestDto) {
        try {
            if (!mobileNoRequestDto.isRequiredAvailable()) {
                return getErrorResponse(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
            WhatsAppAvailabilityResponseDto numberOnWhatsAppResponseDto = whatsappApiClient.isNumberOnWhatsApp(mobileNoRequestDto);
            return getSuccessResponse(numberOnWhatsAppResponseDto, SuccessResponseStatusType.MOBILE_NUMBER_AVAILABLE_ON_WHATSAPP);
        } catch (UtilServiceException e) {
            logger.error("Failed to check WhatsApp availability for mobile number: {}", mobileNoRequestDto.getNo(), e);
            return getInternalServerError();
        }
    }
}