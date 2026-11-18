package com.kupi.cc.auth.controller;

import com.kupi.cc.auth.configuration.Translator;
import com.kupi.cc.auth.domain.request.DemoVideoRequestDto;
import com.kupi.cc.auth.domain.response.DemoVideoResponseDto;
import com.kupi.cc.auth.enums.ErrorResponseStatusType;
import com.kupi.cc.auth.enums.SuccessResponseStatusType;
import com.kupi.cc.auth.exception.AuthServiceException;
import com.kupi.cc.auth.service.DemoVideoService;
import com.kupi.cc.auth.wrapper.ResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Demo Video Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/demo-video")
public class DemoVideoController extends Controller {

    private final DemoVideoService demoVideoService;

    @Autowired
    public DemoVideoController(Translator translator, DemoVideoService demoVideoService) {
        super(translator);
        this.demoVideoService = demoVideoService;
    }

    /**
     * This method creates a demo video
     *
     * @param userId userId from header
     * @param demoVideoRequestDto demoVideoRequestDto
     * @return created demo video
     */
    @Secured({ADMIN_ROLE})
    @PostMapping(path = "", consumes = APPLICATION_JSON_UTF_8, produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> createDemoVideo(@RequestHeader(name = HEADER_USER_ID) String userId,
                                                             @RequestBody DemoVideoRequestDto demoVideoRequestDto) {
        try {
            if (demoVideoRequestDto.isRequiredAvailable()) {
                DemoVideoResponseDto responseDto = demoVideoService.createDemoVideo(demoVideoRequestDto);
                log.debug("Successfully created demo video: {} by userId: {}", responseDto.toLogJson(), userId);
                return getSuccessResponse(SuccessResponseStatusType.CREATE_DEMO_VIDEO, responseDto);
            } else {
                return getBadRequestError(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
        } catch (AuthServiceException e) {
            log.error("Creating demo video failed for userId: {}, request: {}", userId, 
                    demoVideoRequestDto.toLogJson(), e);
            return getInternalServerError();
        }
    }

    /**
     * This method returns the active demo video
     *
     * @param userId userId from header
     * @return active demo video
     */
    @GetMapping(path = "", produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> getActiveDemoVideo(@RequestHeader(name = HEADER_USER_ID) String userId) {
        try {
            Optional<DemoVideoResponseDto> activeVideo = demoVideoService.getActiveDemoVideo();
            if (activeVideo.isPresent()) {
                log.debug("Successfully returned active demo video for userId: {}", userId);
                return getSuccessResponse(SuccessResponseStatusType.RETURNED_DEMO_VIDEO, activeVideo.get());
            } else {
                log.debug("No active demo video found for userId: {}", userId);
                return getSuccessResponse(SuccessResponseStatusType.RETURNED_DEMO_VIDEO, null);
            }
        } catch (AuthServiceException e) {
            log.error("Getting active demo video failed for userId: {}", userId, e);
            return getInternalServerError();
        }
    }
}

