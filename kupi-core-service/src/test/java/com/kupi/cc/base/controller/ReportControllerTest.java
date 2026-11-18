package com.kupi.cc.base.controller;

import com.kupi.cc.base.configuration.ResourceBundleMessageSourceBean;
import com.kupi.cc.base.configuration.Translator;
import com.kupi.cc.base.domain.entity.DealSearchIndex;
import com.kupi.cc.base.domain.request.BulkUserRequestDto;
import com.kupi.cc.base.domain.response.DealViewCountResponse;
import com.kupi.cc.base.domain.response.MerchantBusinessResponseDto;
import com.kupi.cc.base.enums.*;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.service.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Errors;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * This class tests {@link ReportController} class
 */
class ReportControllerTest {

    private static final UserType USER_TYPE_MERCHANT = UserType.MERCHANT;
    private static final String USER_ID_HEADER = "User-Id";
    private static final String USER_ID = "uid-123456789";
    private static final String TIME_ZONE_HEADER = "Time-Zone";
    private static final String TIME_ZONE = "Asia/Colombo";
    private static final String OPTION = "THIS-MONTH";
    private static final String NULL = "NULL";
    private static final String DEAL_SEARCH_ID = "did-123456789";
    private static final long VIEW_COUNT = 2654L;
    private static final String SUCCESS_STATUS = "SUCCESS";
    private static final String ERROR_STATUS = "ERROR";
    private static final String FAILED = "failed";
    private static final String APPLICATION_JSON = "application/json";

    private static final String GET_TOP_TEN_DEAL_VS_VIEW_COUNT_URI = "/api/v1/reports/deal/audience-reach/top-ten/{userType}/{merchantId}/{option}";

    @Mock
    private Translator translator;
    @Mock
    private ReportService reportService;
    @Mock
    private AuthUserService authUserService;
    @Mock
    private DealSearchService dealSearchService;
    @Mock
    private CategoryService categoryService;
    Locale locale = LocaleContextHolder.getLocale();
    private MockMvc mockMvc;
    private ResourceBundleMessageSource resourceBundleMessageSource;

    private final ResourceBundleMessageSourceBean resourceBundleMessageSourceBean = new ResourceBundleMessageSourceBean();

    @BeforeEach
    void setUp() {
        initMocks(this);
        ReportController reportController = new ReportController(translator, reportService, authUserService,
                dealSearchService, categoryService);
        org.springframework.validation.Validator noOpValidator = new org.springframework.validation.Validator() {
            @Override
            public boolean supports(Class<?> clazz) {
                return true;
            }

            @Override
            public void validate(Object target, Errors errors) {
                // No-op validator
            }
        };
        mockMvc = MockMvcBuilders.standaloneSetup(reportController)
                .setValidator(noOpValidator)
                .build();
        this.resourceBundleMessageSource = resourceBundleMessageSourceBean.messageSource();
    }

    @AfterEach
    void tearDown() {
    }

    /**
     * Start of tests for get top ten deal vs view count
     * Api context: api/v1/reports/deal/audience-reach/top-ten/{userType}/{merchantId}/{option}
     */
    @Test
    void Should_ReturnOk_When_GettingTopTenDealVsViewCountIsSuccessful() throws Exception {
        when(reportService.getTopDealsOrCategoryReport(any(GraphDateOption.class), anyString(), anyString(),
                any(ReportType.class), any(ReportQueryAttribute.class))).thenReturn(new ArrayList<>());
        when(dealSearchService.getDealSetByIds(anyList())).thenReturn(getSampleDealViewCountResponseList());
        when(authUserService.getMerchantMap(any(BulkUserRequestDto.class), any(UserType.class)))
                .thenReturn(getSampleMerchantBusinessResponseDtoMap());

        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(SuccessResponseStatusType
                .READ_DEAL_VIEW_GRAPH.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.READ_DEAL_VIEW_GRAPH
                .getCode()))).thenReturn(successMessage);
        String uri = GET_TOP_TEN_DEAL_VS_VIEW_COUNT_URI.replace("{userType}", USER_TYPE_MERCHANT.name())
                .replace("{merchantId}", USER_ID).replace("{option}", OPTION);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.READ_DEAL_VIEW_GRAPH.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.READ_DEAL_VIEW_GRAPH.getCode()))
                .andExpect(jsonPath("$.data.views[0].deal.id").value(DEAL_SEARCH_ID))
                .andExpect(jsonPath("$.data.views[0].merchant.id").value(USER_ID))
                .andExpect(jsonPath("$.data.views[0].viewCount").value(VIEW_COUNT))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_GettingTopTenDealVsViewCountForInvalidDateOption() throws Exception {
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_DATE_OPTION.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_DATE_OPTION.getCode())))
                .thenReturn(invalidMessage);
        String uri = GET_TOP_TEN_DEAL_VS_VIEW_COUNT_URI.replace("{userType}", USER_TYPE_MERCHANT.name())
                .replace("{merchantId}", USER_ID).replace("{option}", NULL);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_DATE_OPTION.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_DATE_OPTION.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_GettingTopTenDealVsViewCountIsFailed() throws Exception {
        when(reportService.getTopDealsOrCategoryReport(any(GraphDateOption.class), anyString(), anyString(),
                any(ReportType.class), any(ReportQueryAttribute.class))).thenReturn(new ArrayList<>());
        when(dealSearchService.getDealSetByIds(anyList())).thenThrow(new KupiCoreException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                .getCode()))).thenReturn(invalidMessage);
        String uri = GET_TOP_TEN_DEAL_VS_VIEW_COUNT_URI.replace("{userType}", USER_TYPE_MERCHANT.name())
                .replace("{merchantId}", USER_ID).replace("{option}", OPTION);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    private Map<String, MerchantBusinessResponseDto> getSampleMerchantBusinessResponseDtoMap() {
        MerchantBusinessResponseDto dto = getSampleMerchantBusinessResponseDto();
        Map<String, MerchantBusinessResponseDto> map = new HashMap<>();
        map.put(dto.getId(), dto);
        return map;
    }

    private MerchantBusinessResponseDto getSampleMerchantBusinessResponseDto() {
        MerchantBusinessResponseDto dto = new MerchantBusinessResponseDto();
        dto.setId(USER_ID);
        return dto;
    }

    private List<DealViewCountResponse> getSampleDealViewCountResponseList() {
        DealSearchIndex dealSearchIndex = new DealSearchIndex();
        dealSearchIndex.setId(DEAL_SEARCH_ID);
        dealSearchIndex.setMerchantId(USER_ID);
        DealViewCountResponse dealViewCountResponse = new DealViewCountResponse(dealSearchIndex, "", VIEW_COUNT, "",
                USER_TYPE_MERCHANT, 0);
        List<DealViewCountResponse> dealViewCountResponseList = new ArrayList<>();
        dealViewCountResponseList.add(dealViewCountResponse);
        return dealViewCountResponseList;
    }
}