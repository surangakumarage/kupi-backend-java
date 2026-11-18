package com.kupi.cc.auth.controller;

import com.kupi.cc.auth.configuration.ResourceBundleMessageSourceBean;
import com.kupi.cc.auth.configuration.Translator;
import com.kupi.cc.auth.domain.request.DemoVideoRequestDto;
import com.kupi.cc.auth.domain.response.DemoVideoResponseDto;
import com.kupi.cc.auth.enums.ErrorResponseStatusType;
import com.kupi.cc.auth.enums.SuccessResponseStatusType;
import com.kupi.cc.auth.exception.AuthServiceException;
import com.kupi.cc.auth.service.DemoVideoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Locale;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This class tests the {@link DemoVideoController} class.
 */
class DemoVideoControllerTest {

    private static final String USER_ID_HEADER = "User-Id";
    private static final String USER_ID = "uid-123456789";
    private static final String DEMO_VIDEO_ID = "dvid-1234567890";
    private static final String DEMO_VIDEO_NAME = "Introduction Video";
    private static final String DEMO_VIDEO_URL = "https://example.com/video.mp4";
    private static final String SUCCESS_STATUS = "SUCCESS";
    private static final String ERROR_STATUS = "ERROR";
    private static final String FAILED = "failed";
    private static final String APPLICATION_JSON_UTF_8 = "application/json;charset=UTF-8";
    private static final String CREATE_DEMO_VIDEO_URI = "/api/v1/demo-video";
    private static final String GET_ACTIVE_DEMO_VIDEO_URI = "/api/v1/demo-video";

    private final ResourceBundleMessageSourceBean resourceBundleMessageSourceBean = new ResourceBundleMessageSourceBean();
    Locale locale = LocaleContextHolder.getLocale();
    @Mock
    private Translator translator;
    @Mock
    private DemoVideoService demoVideoService;
    private MockMvc mockMvc;
    private ResourceBundleMessageSource resourceBundleMessageSource;

    @BeforeEach
    void setUp() {
        initMocks(this);
        DemoVideoController demoVideoController = new DemoVideoController(translator, demoVideoService);
        mockMvc = MockMvcBuilders.standaloneSetup(demoVideoController).build();
        this.resourceBundleMessageSource = resourceBundleMessageSourceBean.messageSource();
    }

    /**
     * Create demo video
     */
    @Test
    void Should_ReturnOk_When_CreatingDemoVideoIsSuccessful() throws Exception {
        DemoVideoResponseDto responseDto = getSampleDemoVideoResponseDto();

        doReturn(responseDto).when(demoVideoService).createDemoVideo(any(DemoVideoRequestDto.class));
        String successMessage = resourceBundleMessageSource.getMessage(SuccessResponseStatusType.CREATE_DEMO_VIDEO
                .getCode(), null, locale);
        when(translator.toLocale(SuccessResponseStatusType.CREATE_DEMO_VIDEO.getCode())).thenReturn(successMessage);

        mockMvc.perform(MockMvcRequestBuilders.post(CREATE_DEMO_VIDEO_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + DEMO_VIDEO_NAME + "\",\"videoUrl\":\"" + DEMO_VIDEO_URL + "\",\"active\":true}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.CREATE_DEMO_VIDEO.getMessage()))
                .andExpect(jsonPath("$.data.id").value(DEMO_VIDEO_ID))
                .andExpect(jsonPath("$.data.name").value(DEMO_VIDEO_NAME))
                .andExpect(jsonPath("$.data.videoUrl").value(DEMO_VIDEO_URL))
                .andExpect(jsonPath("$.data.active").value(true))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_CreatingDemoVideoWithMissingFields() throws Exception {
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .MISSING_REQUIRED_FIELDS.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode())))
                .thenReturn(invalidMessage);

        mockMvc.perform(MockMvcRequestBuilders.post(CREATE_DEMO_VIDEO_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"videoUrl\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_CreatingDemoVideoIsFailed() throws Exception {
        when(demoVideoService.createDemoVideo(any(DemoVideoRequestDto.class))).thenThrow(new AuthServiceException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);

        mockMvc.perform(MockMvcRequestBuilders.post(CREATE_DEMO_VIDEO_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + DEMO_VIDEO_NAME + "\",\"videoUrl\":\"" + DEMO_VIDEO_URL + "\",\"active\":true}"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    /**
     * Get active demo video
     */
    @Test
    void Should_ReturnOk_When_GettingActiveDemoVideoIsSuccessful() throws Exception {
        DemoVideoResponseDto responseDto = getSampleDemoVideoResponseDto();
        when(demoVideoService.getActiveDemoVideo()).thenReturn(Optional.of(responseDto));

        String successMessage = resourceBundleMessageSource.getMessage(SuccessResponseStatusType.RETURNED_DEMO_VIDEO
                .getCode(), null, locale);
        when(translator.toLocale(SuccessResponseStatusType.RETURNED_DEMO_VIDEO.getCode())).thenReturn(successMessage);

        mockMvc.perform(MockMvcRequestBuilders.get(GET_ACTIVE_DEMO_VIDEO_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.RETURNED_DEMO_VIDEO.getMessage()))
                .andExpect(jsonPath("$.data.id").value(DEMO_VIDEO_ID))
                .andExpect(jsonPath("$.data.name").value(DEMO_VIDEO_NAME))
                .andExpect(jsonPath("$.data.videoUrl").value(DEMO_VIDEO_URL))
                .andExpect(jsonPath("$.data.active").value(true))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnOkWithNullData_When_NoActiveDemoVideoFound() throws Exception {
        when(demoVideoService.getActiveDemoVideo()).thenReturn(Optional.empty());

        String successMessage = resourceBundleMessageSource.getMessage(SuccessResponseStatusType.RETURNED_DEMO_VIDEO
                .getCode(), null, locale);
        when(translator.toLocale(SuccessResponseStatusType.RETURNED_DEMO_VIDEO.getCode())).thenReturn(successMessage);

        mockMvc.perform(MockMvcRequestBuilders.get(GET_ACTIVE_DEMO_VIDEO_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.RETURNED_DEMO_VIDEO.getMessage()))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_GettingActiveDemoVideoIsFailed() throws Exception {
        when(demoVideoService.getActiveDemoVideo()).thenThrow(new AuthServiceException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);

        mockMvc.perform(MockMvcRequestBuilders.get(GET_ACTIVE_DEMO_VIDEO_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }


    private DemoVideoResponseDto getSampleDemoVideoResponseDto() {
        DemoVideoResponseDto responseDto = new DemoVideoResponseDto();
        responseDto.setId(DEMO_VIDEO_ID);
        responseDto.setName(DEMO_VIDEO_NAME);
        responseDto.setVideoUrl(DEMO_VIDEO_URL);
        responseDto.setActive(true);
        return responseDto;
    }
}

