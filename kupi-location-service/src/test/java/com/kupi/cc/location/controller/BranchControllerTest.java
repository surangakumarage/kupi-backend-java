package com.kupi.cc.location.controller;

import com.kupi.cc.location.configuration.ResourceBundleMessageSourceBean;
import com.kupi.cc.location.configuration.Translator;
import com.kupi.cc.location.domain.entity.ApprovedBranch;
import com.kupi.cc.location.domain.entity.Branch;
import com.kupi.cc.location.domain.entity.BranchSearchIndex;
import com.kupi.cc.location.domain.request.*;
import com.kupi.cc.location.domain.response.DetailBranchResponseDto;
import com.kupi.cc.location.enums.*;
import com.kupi.cc.location.exception.*;
import com.kupi.cc.location.service.BranchService;
import com.kupi.cc.location.util.Validator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This class tests {@link BranchController} class
 */
class BranchControllerTest {

    private static final String USER_ID_HEADER = "User-Id";
    private static final String USER_ID = "uid-123456789";
    private static final String AUTH_TOKEN_HEADER = "Auth-Token";
    private static final String AUTH_TOKEN = "Bearer192f0c96-dd19-404e-8217-a8fd1cc81d55";
    private static final String BRANCH_ID = "brid-123456789";
    private static final String SEARCH_TERM = "Softlogic";
    private static final String COMMENT = "COMMENT";
    private static final String NAME = "Karim Benzema";
    private static final String ADDRESS_VALUE = "ADDRESS";
    private static final String LOCATION_MAP_URL = "LOCATION_MAP_URL";
    private static final String GLOBAL_CODE = "GLOBAL_CODE";
    private static final String LONG_NAME = "LONG_NAME";
    private static final String SHORT_NAME = "SHORT_NAME";
    private static final String PROOF_URL = "https://cleancodeonline.com";
    private static final String CONTACT_NO = "12346789";
    private static final int PAGE = 0;
    private static final int SIZE = 1;
    private static final double LATITUDE = 8.165243;
    private static final double LONGITUDE = 1.54821354;
    private static final String SUCCESS_STATUS = "SUCCESS";
    private static final String ERROR_STATUS = "ERROR";
    private static final String DEFAULT_SORT = "updatedAt";
    private static final String FAILED = "failed";
    private static final String APPLICATION_JSON = "application/json";
    private static final String GET_BRANCH_LIST_URI = "/api/v1/branch/{userId}/{page}/{size}/search/{searchTerm}";
    private static final String GET_PENDING_BRANCH_LIST_URI = "/api/v1/branch/{userType}/pending/{page}/{size}/search/{searchTerm}";
    private static final String APPROVE_OR_REJECT_BRANCH_URI = "/api/v1/branch/approve";
    private static final String VERIFY_ADDRESS_CONTACT_NO_URI = "/api/v1/branch/{verificationType}/verify";
    private static final String CREATE_BRANCH_URI = "/api/v1/branch/{userType}";
    private static final String UPDATE_BRANCH_URI = "/api/v1/branch";
    private static final String GET_ACTIVE_BRANCH_LIST_URI = "/api/v1/branch/active/{userId}/{page}/{size}/search/{searchTerm}";
    private static final String BRANCH_BY_BRANCH_ID_URI = "/api/v1/branch/{branchId}";

    private final ResourceBundleMessageSourceBean resourceBundleMessageSourceBean = new ResourceBundleMessageSourceBean();
    Locale locale = LocaleContextHolder.getLocale();
    @Mock
    private Translator translator;
    @Mock
    private BranchService branchService;
    @Mock
    private Validator validator;
    private MockMvc mockMvc;
    private ResourceBundleMessageSource resourceBundleMessageSource;

    @BeforeEach
    void setUp() {
        initMocks(this);
        BranchController branchController = new BranchController(translator, branchService, validator);
        mockMvc = MockMvcBuilders.standaloneSetup(branchController).build();
        this.resourceBundleMessageSource = resourceBundleMessageSourceBean.messageSource();
    }

    @AfterEach
    void tearDown() {
    }

    /**
     * Start of tests for get branch list
     * Api context: api/v1/branch/{userId}/{page}/{size}/search/{searchTerm}
     */
    @Test
    void Should_ReturnOk_When_GettingBranchListIsSuccessful() throws Exception {
        when(branchService.getBranchListByUserId(anyString(), anyString(), any(Pageable.class)))
                .thenReturn(getSampleBranchPage());

        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(SuccessResponseStatusType
                .READ_BRANCH_LIST.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.READ_BRANCH_LIST.getCode())))
                .thenReturn(successMessage);
        String uri = GET_BRANCH_LIST_URI.replace("{userId}", USER_ID)
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.READ_BRANCH_LIST.getMessage()))
                .andExpect(jsonPath("$.data.branches[0].id").value(BRANCH_ID))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_GettingBranchListIsFailed() throws Exception {
        when(branchService.getBranchListByUserId(anyString(), anyString(), any(Pageable.class)))
                .thenThrow(new KupiLocationException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);
        String uri = GET_BRANCH_LIST_URI.replace("{userId}", USER_ID)
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
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

    /**
     * Start of tests for get pending branch list
     * Api context: api/v1/branch/{userType}/pending/{page}/{size}/search/{searchTerm}
     */
    @Test
    void Should_ReturnOk_When_GettingPendingBranchListIsSuccessful() throws Exception {
        when(branchService.getPendingBranchList(any(UserType.class), anyString(), any(Pageable.class)))
                .thenReturn(getSampleBranchSearchIndexPage());

        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(SuccessResponseStatusType
                .BRANCH_LIST.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.BRANCH_LIST.getCode())))
                .thenReturn(successMessage);
        String uri = GET_PENDING_BRANCH_LIST_URI.replace("{userType}", UserType.MERCHANT.toString())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.BRANCH_LIST.getMessage()))
                .andExpect(jsonPath("$.data.branches[0].id").value(BRANCH_ID))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_GettingPendingBranchListIsFailed() throws Exception {
        when(branchService.getPendingBranchList(any(UserType.class), anyString(), any(Pageable.class)))
                .thenThrow(new KupiLocationException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);
        String uri = GET_PENDING_BRANCH_LIST_URI.replace("{userType}", UserType.MERCHANT.toString())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
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

    /**
     * Start of tests for approve or reject branch
     * Api context: api/v1/branch/approve
     */
    @Test
    void Should_ReturnOk_When_ApproveBranchIsSuccessful() throws Exception {
        doNothing().when(branchService).approveOrRejectBranch(any(ApprovalStatusUpdateRequestDto.class), anyString());

        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(SuccessResponseStatusType
                .APPROVED_BRANCH.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.APPROVED_BRANCH.getCode())))
                .thenReturn(successMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(APPROVE_OR_REJECT_BRANCH_URI)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(getSampleApprovalStatusUpdateRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.APPROVED_BRANCH.getMessage()))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnOk_When_RejectBranchIsSuccessful() throws Exception {
        doNothing().when(branchService).approveOrRejectBranch(any(ApprovalStatusUpdateRequestDto.class), anyString());
        ApprovalStatusUpdateRequestDto approvalStatusUpdateRequestDto = getSampleApprovalStatusUpdateRequestDto();
        approvalStatusUpdateRequestDto.setAction(ApprovalAction.REJECT);
        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(SuccessResponseStatusType
                .REJECTED_BRANCH.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.REJECTED_BRANCH.getCode())))
                .thenReturn(successMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(APPROVE_OR_REJECT_BRANCH_URI)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(approvalStatusUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.REJECTED_BRANCH.getMessage()))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_ApproveOrRejectBranchForMissingRequiredFields() throws Exception {
        ApprovalStatusUpdateRequestDto approvalStatusUpdateRequestDto = getSampleApprovalStatusUpdateRequestDto();
        approvalStatusUpdateRequestDto.setId(null);

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .MISSING_REQUIRED_FIELDS.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(APPROVE_OR_REJECT_BRANCH_URI)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(approvalStatusUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_ApproveOrRejectBranchForInvalidBranchId() throws Exception {
        doThrow(new InvalidBranchException(FAILED)).when(branchService)
                .approveOrRejectBranch(any(ApprovalStatusUpdateRequestDto.class), anyString());

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_BRANCH_ID.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_BRANCH_ID.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(APPROVE_OR_REJECT_BRANCH_URI)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(getSampleApprovalStatusUpdateRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_BRANCH_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_BRANCH_ID.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_ApproveOrRejectBranchIsFailed() throws Exception {
        doThrow(new KupiLocationException(FAILED)).when(branchService)
                .approveOrRejectBranch(any(ApprovalStatusUpdateRequestDto.class), anyString());

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(APPROVE_OR_REJECT_BRANCH_URI)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(getSampleApprovalStatusUpdateRequestDto().toJson())
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

    /**
     * Start of tests for verify address contactNo
     * Api context: api/v1/branch/{verificationType}/verify
     */
    @Test
    void Should_ReturnOk_When_VerifyAddressContactNoIsSuccessful() throws Exception {
        doNothing().when(branchService).verifyAddressOrContactNo(any(VerifyAddressContactRequestDto.class),
                any(VerificationType.class));

        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(SuccessResponseStatusType
                .VERIFIED.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.VERIFIED.getCode())))
                .thenReturn(successMessage);
        String uri = VERIFY_ADDRESS_CONTACT_NO_URI.replace("{verificationType}", VerificationType.CONTACT
                .toString());
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                        .content(getSampleVerifyAddressContactRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.VERIFIED.getMessage()))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_VerifyAddressContactNoForMissingRequiredFields() throws Exception {
        VerifyAddressContactRequestDto verifyAddressContactRequestDto = getSampleVerifyAddressContactRequestDto();
        verifyAddressContactRequestDto.setId(null);

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .MISSING_REQUIRED_FIELDS.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode())))
                .thenReturn(invalidMessage);
        String uri = VERIFY_ADDRESS_CONTACT_NO_URI.replace("{verificationType}", VerificationType.CONTACT
                .toString());
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                        .content(verifyAddressContactRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_VerifyAddressContactNoForInvalidBranchId() throws Exception {
        doThrow(new InvalidBranchException(FAILED)).when(branchService)
                .verifyAddressOrContactNo(any(VerifyAddressContactRequestDto.class), any(VerificationType.class));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_BRANCH_ID.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_BRANCH_ID.getCode())))
                .thenReturn(invalidMessage);
        String uri = VERIFY_ADDRESS_CONTACT_NO_URI.replace("{verificationType}", VerificationType.CONTACT
                .toString());
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                        .content(getSampleVerifyAddressContactRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_BRANCH_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_BRANCH_ID.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_VerifyAddressContactNoIsFailed() throws Exception {
        doThrow(new KupiLocationException(FAILED)).when(branchService)
                .verifyAddressOrContactNo(any(VerifyAddressContactRequestDto.class), any(VerificationType.class));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);
        String uri = VERIFY_ADDRESS_CONTACT_NO_URI.replace("{verificationType}", VerificationType.CONTACT
                .toString());
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                        .content(getSampleVerifyAddressContactRequestDto().toJson())
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

    /**
     * Start of tests for create branch
     * Api context: api/v1/branch/{userType}
     */
    @Test
    void Should_ReturnOk_When_CreatingBranchIsSuccessful() throws Exception {
        when(validator.isValidUrl(LOCATION_MAP_URL)).thenReturn(true);
        doNothing().when(branchService).createBranch(any(Branch.class), any(UserType.class));

        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(SuccessResponseStatusType
                .CREATE_BRANCH.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.CREATE_BRANCH.getCode())))
                .thenReturn(successMessage);
        String uri = CREATE_BRANCH_URI.replace("{userType}", UserType.MERCHANT.toString());
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(getSampleBranchRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.CREATE_BRANCH.getMessage()))
                .andExpect(jsonPath("$.data.name").value(NAME))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_CreatingBranchForUnsupportedUserType() throws Exception {
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .UNSUPPORTED_USER_TYPE_ACTION.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getCode())))
                .thenReturn(invalidMessage);
        String uri = CREATE_BRANCH_URI.replace("{userType}", UserType.ADMIN.toString());
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(getSampleBranchRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_CreatingBranchForMissingRequiredFields() throws Exception {
        BranchRequestDto branchRequestDto = getSampleBranchRequestDto();
        branchRequestDto.setName(null);

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .MISSING_REQUIRED_FIELDS.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode())))
                .thenReturn(invalidMessage);
        String uri = CREATE_BRANCH_URI.replace("{userType}", UserType.MERCHANT.toString());
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(branchRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_CreatingBranchForInvalidMapUrl() throws Exception {
        when(validator.isValidUrl(LOCATION_MAP_URL)).thenReturn(false);

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_MAP_URL.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_MAP_URL.getCode())))
                .thenReturn(invalidMessage);
        String uri = CREATE_BRANCH_URI.replace("{userType}", UserType.MERCHANT.toString());
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(getSampleBranchRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_MAP_URL.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_MAP_URL.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_CreatingBranchForInvalidLatitude() throws Exception {
        BranchRequestDto branchRequestDto = getSampleBranchRequestDto();
        LocationRequestDto locationRequestDto = getSampleLocationRequestDto();
        locationRequestDto.setLatitude(-100_000.0);
        branchRequestDto.setLocation(locationRequestDto);

        when(validator.isValidUrl(LOCATION_MAP_URL)).thenReturn(true);

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_LATITUDE.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_LATITUDE.getCode())))
                .thenReturn(invalidMessage);
        String uri = CREATE_BRANCH_URI.replace("{userType}", UserType.MERCHANT.toString());
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(branchRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_LATITUDE.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_LATITUDE.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_CreatingBranchForInvalidLongitude() throws Exception {
        BranchRequestDto branchRequestDto = getSampleBranchRequestDto();
        LocationRequestDto locationRequestDto = getSampleLocationRequestDto();
        locationRequestDto.setLongitude(-200_000.0);
        branchRequestDto.setLocation(locationRequestDto);

        when(validator.isValidUrl(LOCATION_MAP_URL)).thenReturn(true);

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_LONGITUDE.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_LONGITUDE.getCode())))
                .thenReturn(invalidMessage);
        String uri = CREATE_BRANCH_URI.replace("{userType}", UserType.MERCHANT.toString());
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(branchRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_LONGITUDE.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_LONGITUDE.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_CreatingBranchForBusinessProfileNotFound() throws Exception {
        when(validator.isValidUrl(LOCATION_MAP_URL)).thenReturn(true);
        doThrow(new InvalidUserException(FAILED)).when(branchService).createBranch(any(Branch.class), any(UserType.class));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .BUSINESS_PROFILE_NOT_FOUND.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.BUSINESS_PROFILE_NOT_FOUND.getCode())))
                .thenReturn(invalidMessage);
        String uri = CREATE_BRANCH_URI.replace("{userType}", UserType.MERCHANT.toString());
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(getSampleBranchRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.BUSINESS_PROFILE_NOT_FOUND.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.BUSINESS_PROFILE_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_CreatingBranchForExistingBranchName() throws Exception {
        when(validator.isValidUrl(LOCATION_MAP_URL)).thenReturn(true);
        doThrow(new ExistingNameException(FAILED)).when(branchService).createBranch(any(Branch.class), any(UserType.class));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .EXISTING_BRANCH_NAME.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.EXISTING_BRANCH_NAME.getCode())))
                .thenReturn(invalidMessage);
        String uri = CREATE_BRANCH_URI.replace("{userType}", UserType.MERCHANT.toString());
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(getSampleBranchRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.EXISTING_BRANCH_NAME.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.EXISTING_BRANCH_NAME.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_CreatingBranchForExistingBranchAddress() throws Exception {
        when(validator.isValidUrl(LOCATION_MAP_URL)).thenReturn(true);
        doThrow(new ExistingAddressException(FAILED)).when(branchService).createBranch(any(Branch.class), any(UserType.class));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .EXISTING_BRANCH_ADDRESS.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.EXISTING_BRANCH_ADDRESS.getCode())))
                .thenReturn(invalidMessage);
        String uri = CREATE_BRANCH_URI.replace("{userType}", UserType.MERCHANT.toString());
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(getSampleBranchRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.EXISTING_BRANCH_ADDRESS.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.EXISTING_BRANCH_ADDRESS.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_CreatingBranchIsFailed() throws Exception {
        when(validator.isValidUrl(LOCATION_MAP_URL)).thenReturn(true);
        doThrow(new KupiLocationException(FAILED)).when(branchService).createBranch(any(Branch.class), any(UserType.class));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);
        String uri = CREATE_BRANCH_URI.replace("{userType}", UserType.MERCHANT.toString());
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(getSampleBranchRequestDto().toJson())
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

    /**
     * Start of tests for update branch
     * Api context: api/v1/branch
     */
    @Test
    void Should_ReturnOk_When_UpdatingBranchIsSuccessful() throws Exception {
        when(validator.isValidUrl(LOCATION_MAP_URL)).thenReturn(true);
        when(branchService.updateBranch(any(BranchUpdateRequestDto.class))).thenReturn(getSampleBranch());

        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(SuccessResponseStatusType
                .UPDATE_BRANCH.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.UPDATE_BRANCH.getCode())))
                .thenReturn(successMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_BRANCH_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(getSampleBranchUpdateRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.UPDATE_BRANCH.getMessage()))
                .andExpect(jsonPath("$.data.id").value(BRANCH_ID))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UpdatingBranchForMissingRequiredFields() throws Exception {
        BranchUpdateRequestDto branchUpdateRequestDto = getSampleBranchUpdateRequestDto();
        branchUpdateRequestDto.setId(null);

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .MISSING_REQUIRED_FIELDS.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_BRANCH_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(branchUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UpdatingBranchForInvalidMapUrl() throws Exception {
        when(validator.isValidUrl(LOCATION_MAP_URL)).thenReturn(false);

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_MAP_URL.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_MAP_URL.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_BRANCH_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(getSampleBranchUpdateRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_MAP_URL.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_MAP_URL.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UpdatingBranchForInvalidLatitude() throws Exception {
        BranchUpdateRequestDto branchUpdateRequestDto = getSampleBranchUpdateRequestDto();
        LocationRequestDto locationRequestDto = getSampleLocationRequestDto();
        locationRequestDto.setLatitude(-100_000.0);
        branchUpdateRequestDto.setLocation(locationRequestDto);

        when(validator.isValidUrl(LOCATION_MAP_URL)).thenReturn(true);

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_LATITUDE.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_LATITUDE.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_BRANCH_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(branchUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_LATITUDE.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_LATITUDE.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UpdatingBranchForInvalidLongitude() throws Exception {
        BranchUpdateRequestDto branchUpdateRequestDto = getSampleBranchUpdateRequestDto();
        LocationRequestDto locationRequestDto = getSampleLocationRequestDto();
        locationRequestDto.setLongitude(-200_000.0);
        branchUpdateRequestDto.setLocation(locationRequestDto);

        when(validator.isValidUrl(LOCATION_MAP_URL)).thenReturn(true);

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_LONGITUDE.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_LONGITUDE.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_BRANCH_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(branchUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_LONGITUDE.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_LONGITUDE.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UpdatingBranchForInvalidBranchId() throws Exception {
        when(validator.isValidUrl(LOCATION_MAP_URL)).thenReturn(true);
        when(branchService.updateBranch(any(BranchUpdateRequestDto.class)))
                .thenThrow(new InvalidBranchException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_BRANCH_ID.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_BRANCH_ID.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_BRANCH_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(getSampleBranchUpdateRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_BRANCH_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_BRANCH_ID.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UpdatingBranchForExistingBranchName() throws Exception {
        when(validator.isValidUrl(LOCATION_MAP_URL)).thenReturn(true);
        when(branchService.updateBranch(any(BranchUpdateRequestDto.class)))
                .thenThrow(new ExistingNameException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .EXISTING_BRANCH_NAME.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.EXISTING_BRANCH_NAME.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_BRANCH_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(getSampleBranchUpdateRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.EXISTING_BRANCH_NAME.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.EXISTING_BRANCH_NAME.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UpdatingBranchForExistingBranchAddress() throws Exception {
        when(validator.isValidUrl(LOCATION_MAP_URL)).thenReturn(true);
        when(branchService.updateBranch(any(BranchUpdateRequestDto.class)))
                .thenThrow(new ExistingAddressException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .EXISTING_BRANCH_ADDRESS.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.EXISTING_BRANCH_ADDRESS.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_BRANCH_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(getSampleBranchUpdateRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.EXISTING_BRANCH_ADDRESS.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.EXISTING_BRANCH_ADDRESS.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_UpdatingBranchIsFailed() throws Exception {
        when(validator.isValidUrl(LOCATION_MAP_URL)).thenReturn(true);
        when(branchService.updateBranch(any(BranchUpdateRequestDto.class)))
                .thenThrow(new KupiLocationException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_BRANCH_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(getSampleBranchUpdateRequestDto().toJson())
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

    /**
     * Start of tests for get active branch list
     * Api context: api/v1/branch/active/{userId}/{page}/{size}/search/{searchTerm}
     */
    @Test
    void Should_ReturnOk_When_GettingActiveBranchListIsSuccessful() throws Exception {
        when(branchService.getApprovedBranchListByUserId(anyString(), anyString(), any(Pageable.class)))
                .thenReturn(getSampleApprovedBranchPage());

        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(SuccessResponseStatusType
                .READ_BRANCH_LIST.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.READ_BRANCH_LIST.getCode())))
                .thenReturn(successMessage);
        String uri = GET_ACTIVE_BRANCH_LIST_URI.replace("{userId}", USER_ID)
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.READ_BRANCH_LIST.getMessage()))
                .andExpect(jsonPath("$.data.branches[0].id").value(BRANCH_ID))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_GettingActiveBranchListIsFailed() throws Exception {
        when(branchService.getApprovedBranchListByUserId(anyString(), anyString(), any(Pageable.class)))
                .thenThrow(new KupiLocationException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);
        String uri = GET_ACTIVE_BRANCH_LIST_URI.replace("{userId}", USER_ID)
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
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

    /**
     * Start of tests for get detail branch
     * Api context: api/v1/branch/{branchId}
     */
    @Test
    void Should_ReturnOk_When_GettingDetailBranchIsSuccessful() throws Exception {
        when(branchService.getBranchDetailById(anyString())).thenReturn(getSampleDetailBranchResponseDto());

        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(SuccessResponseStatusType
                .READ_BRANCH_DETAIL.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.READ_BRANCH_DETAIL.getCode())))
                .thenReturn(successMessage);
        String uri = BRANCH_BY_BRANCH_ID_URI.replace("{branchId}", BRANCH_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.READ_BRANCH_DETAIL.getMessage()))
                .andExpect(jsonPath("$.data.id").value(BRANCH_ID))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_GettingDetailBranchForInvalidBranchId() throws Exception {
        when(branchService.getBranchDetailById(anyString())).thenThrow(new InvalidBranchException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_BRANCH_ID.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_BRANCH_ID.getCode())))
                .thenReturn(invalidMessage);
        String uri = BRANCH_BY_BRANCH_ID_URI.replace("{branchId}", BRANCH_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_BRANCH_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_BRANCH_ID.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_GettingDetailBranchIsFailed() throws Exception {
        when(branchService.getBranchDetailById(anyString())).thenThrow(new KupiLocationException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);
        String uri = BRANCH_BY_BRANCH_ID_URI.replace("{branchId}", BRANCH_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
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

    /**
     * Start of tests for delete branch
     * Api context: api/v1/branch/{branchId}
     */
    @Test
    void Should_ReturnOk_When_DeletingBranchIsSuccessful() throws Exception {
        doNothing().when(branchService).deteleBranch(anyString());

        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(SuccessResponseStatusType
                .DELETED_BRANCH.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.DELETED_BRANCH.getCode())))
                .thenReturn(successMessage);
        String uri = BRANCH_BY_BRANCH_ID_URI.replace("{branchId}", BRANCH_ID);
        mockMvc.perform(MockMvcRequestBuilders.delete(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.DELETED_BRANCH.getMessage()))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_DeletingBranchForInvalidBranchId() throws Exception {
        doThrow(new InvalidBranchException(FAILED)).when(branchService).deteleBranch(anyString());

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_BRANCH_ID.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_BRANCH_ID.getCode())))
                .thenReturn(invalidMessage);
        String uri = BRANCH_BY_BRANCH_ID_URI.replace("{branchId}", BRANCH_ID);
        mockMvc.perform(MockMvcRequestBuilders.delete(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_BRANCH_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_BRANCH_ID.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_DeletingBranchIsFailed() throws Exception {
        doThrow(new KupiLocationException(FAILED)).when(branchService).deteleBranch(anyString());

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);
        String uri = BRANCH_BY_BRANCH_ID_URI.replace("{branchId}", BRANCH_ID);
        mockMvc.perform(MockMvcRequestBuilders.delete(uri)
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

    private Page<Branch> getSampleBranchPage() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
        List<Branch> branchList = new ArrayList<>();
        branchList.add(getSampleBranch());
        return new PageImpl<>(branchList, pageable, 1);
    }

    private Branch getSampleBranch() {
        Branch branch = new Branch();
        branch.setId(BRANCH_ID);
        return branch;
    }

    private Page<BranchSearchIndex> getSampleBranchSearchIndexPage() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.Direction.DESC, DEFAULT_SORT);

        List<BranchSearchIndex> branchSearchIndexList = new ArrayList<>();
        branchSearchIndexList.add(getSampleBranchSearchIndex());
        return new PageImpl<>(branchSearchIndexList, pageable, 1);
    }

    private BranchSearchIndex getSampleBranchSearchIndex() {
        BranchSearchIndex branchSearchIndex = new BranchSearchIndex();
        branchSearchIndex.setId(BRANCH_ID);
        branchSearchIndex.setUserType(UserType.MERCHANT);
        return branchSearchIndex;
    }

    private ApprovalStatusUpdateRequestDto getSampleApprovalStatusUpdateRequestDto() {
        ApprovalStatusUpdateRequestDto approvalStatusUpdateRequestDto = new ApprovalStatusUpdateRequestDto();
        approvalStatusUpdateRequestDto.setId(BRANCH_ID);
        approvalStatusUpdateRequestDto.setComment(COMMENT);
        approvalStatusUpdateRequestDto.setAction(ApprovalAction.APPROVE);
        return approvalStatusUpdateRequestDto;
    }

    private VerifyAddressContactRequestDto getSampleVerifyAddressContactRequestDto() {
        VerifyAddressContactRequestDto verifyAddressContactRequestDto = new VerifyAddressContactRequestDto();
        verifyAddressContactRequestDto.setId(BRANCH_ID);
        verifyAddressContactRequestDto.setAction(VerificationStatus.VERIFIED);
        return verifyAddressContactRequestDto;
    }

    private BranchRequestDto getSampleBranchRequestDto() {
        BranchRequestDto branchRequestDto = new BranchRequestDto();
        branchRequestDto.setName(NAME);
        branchRequestDto.setUserId(USER_ID);
        branchRequestDto.setAddress(getSampleAddressRequestDto());
        branchRequestDto.setLocation(getSampleLocationRequestDto());
        return branchRequestDto;
    }

    private AddressRequestDto getSampleAddressRequestDto() {
        AddressRequestDto addressRequestDto = new AddressRequestDto();
        addressRequestDto.setValue(ADDRESS_VALUE);
        return addressRequestDto;
    }

    private LocationRequestDto getSampleLocationRequestDto() {
        LocationRequestDto locationRequestDto = new LocationRequestDto();
        locationRequestDto.setLocationMapUrl(LOCATION_MAP_URL);
        locationRequestDto.setLatitude(LATITUDE);
        locationRequestDto.setLongitude(LONGITUDE);
        locationRequestDto.setGlobalCode(GLOBAL_CODE);
        locationRequestDto.setCity(getSampleLocationCity());
        locationRequestDto.setCountry(getSampleLocationCountry());
        return locationRequestDto;
    }

    private LongShortNameRequestDto getSampleLocationCity() {
        LongShortNameRequestDto city = new LongShortNameRequestDto();
        city.setLongName(LONG_NAME);
        city.setShortName(SHORT_NAME);
        return city;
    }

    private LongShortNameRequestDto getSampleLocationCountry() {
        LongShortNameRequestDto country = new LongShortNameRequestDto();
        country.setLongName(LONG_NAME);
        country.setShortName(SHORT_NAME);
        return country;
    }

    private BranchUpdateRequestDto getSampleBranchUpdateRequestDto() {
        BranchUpdateRequestDto branchUpdateRequestDto = new BranchUpdateRequestDto();
        branchUpdateRequestDto.setId(BRANCH_ID);
        branchUpdateRequestDto.setName(NAME);
        branchUpdateRequestDto.setUserId(USER_ID);
        branchUpdateRequestDto.setLocation(getSampleLocationRequestDto());
        branchUpdateRequestDto.setAddress(getSampleAddressRequestDto());
        return branchUpdateRequestDto;
    }

    private Page<ApprovedBranch> getSampleApprovedBranchPage() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).ascending());

        List<ApprovedBranch> approvedBranchList = new ArrayList<>();
        approvedBranchList.add(getSampleApprovedBranch());
        return new PageImpl<>(approvedBranchList, pageable, 1);
    }

    private ApprovedBranch getSampleApprovedBranch() {
        ApprovedBranch approvedBranch = new ApprovedBranch();
        approvedBranch.setId(BRANCH_ID);
        approvedBranch.setAddress(ADDRESS_VALUE);
        approvedBranch.setProofUrl(PROOF_URL);
        approvedBranch.setAddressVerified(true);
        approvedBranch.setContactNo(CONTACT_NO);
        approvedBranch.setContactNoVerified(true);
        return approvedBranch;
    }

    private DetailBranchResponseDto getSampleDetailBranchResponseDto() {
        return new DetailBranchResponseDto(getSampleBranch());
    }
}