package com.kupi.cc.base.controller;

import com.kupi.cc.base.configuration.Translator;
import com.kupi.cc.base.domain.entity.OfferType;
import com.kupi.cc.base.domain.request.OfferTypeCreateRequestDto;
import com.kupi.cc.base.domain.request.OfferTypeUpdateRequestDto;
import com.kupi.cc.base.enums.ErrorResponseStatusType;
import com.kupi.cc.base.enums.SuccessResponseStatusType;
import com.kupi.cc.base.exception.ExistingNameException;
import com.kupi.cc.base.exception.InvalidOfferTypeException;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.exception.UnsupportedDeleteAction;
import com.kupi.cc.base.service.OfferTypeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This class tests for {@link OfferTypeController} class
 */
class OfferTypeControllerTest {

    private static final String OFFER_TYPE_ID = "otid=1256-45875-8547-7852";
    private static final String OFFER_TYPE_NAME = "OFFER_TYPE";
    private static final String USER_ID = "uid-bb801b6a-ae7c-4f81-8876-a722f644fa11";
    private static final String TIME_ZONE = "Asia/Colombo";
    private static final String SUCCESS_STATUS = "SUCCESS";
    private static final String ERROR_STATUS = "ERROR";
    private static final String CREATE_OFFER_TYPE_URI = "/api/v1/request-a-deal/offer-type";
    private static final String UPDATE_OFFER_TYPE_URI = "/api/v1/request-a-deal/offer-type";
    private static final String GET_ALL_OFFER_TYPES_URI = "/api/v1/request-a-deal/offer-type";
    private static final String DELETE_OFFER_TYPE_URI = "/api/v1/request-a-deal/offer-type/{offerTypeId}";

    @Mock
    private OfferTypeService offerTypeService;
    @Mock
    private Translator translator;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        initMocks(this);
        OfferTypeController offerTypeController = new OfferTypeController(translator, offerTypeService);
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
        mockMvc = MockMvcBuilders.standaloneSetup(offerTypeController)
                .setValidator(noOpValidator)
                .build();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_ReturnOK_When_CreateOfferTypeSuccessfully() throws Exception {
        OfferTypeCreateRequestDto offerTypeCreateRequestDto = getSampleOfferTypeCreateRequestDto();
        mockMvc.perform(MockMvcRequestBuilders.post(CREATE_OFFER_TYPE_URI)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .content(offerTypeCreateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.CREATE_OFFER_TYPE.getMessage()))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.CREATE_OFFER_TYPE.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_MissingRequiredFieldsAreProvidedForCreateOfferType() throws Exception {
        OfferTypeCreateRequestDto offerTypeCreateRequestDto = getSampleOfferTypeCreateRequestDto();
        offerTypeCreateRequestDto.setName(null);
        mockMvc.perform(MockMvcRequestBuilders.post(CREATE_OFFER_TYPE_URI)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .content(offerTypeCreateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS
                        .getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS
                        .getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_ExistingOfferTypeNameIsProvidedForCreateOfferType() throws Exception {
        OfferTypeCreateRequestDto offerTypeCreateRequestDto = getSampleOfferTypeCreateRequestDto();
        doThrow(new ExistingNameException("ERROR")).when(offerTypeService).saveOfferType(any(OfferType.class));
        mockMvc.perform(MockMvcRequestBuilders.post(CREATE_OFFER_TYPE_URI)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .content(offerTypeCreateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.EXISTING_OFFER_TYPE_NAME
                        .getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.EXISTING_OFFER_TYPE_NAME
                        .getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_CreateOfferTypeIsFailed() throws Exception {
        OfferTypeCreateRequestDto offerTypeCreateRequestDto = getSampleOfferTypeCreateRequestDto();
        doThrow(new KupiCoreException("ERROR")).when(offerTypeService).saveOfferType(any(OfferType.class));
        mockMvc.perform(MockMvcRequestBuilders.post(CREATE_OFFER_TYPE_URI)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .content(offerTypeCreateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getCode()));
    }

    @Test
    void Should_ReturnOk_When_OfferTypeUpdatedSuccessfully() throws Exception {
        OfferType offerType = getSampleOfferType();
        OfferTypeUpdateRequestDto offerTypeUpdateRequestDto = getSampleOfferTypeUpdateRequestDto();
        when(offerTypeService.getOfferTypeById(OFFER_TYPE_ID)).thenReturn(offerType);
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_OFFER_TYPE_URI)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .content(offerTypeUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.UPDATE_OFFER_TYPE.getMessage()))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.UPDATE_OFFER_TYPE.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_MissingRequiredFieldsAreProvidedForUpdateOfferType() throws Exception {
        OfferTypeUpdateRequestDto offerTypeUpdateRequestDto = getSampleOfferTypeUpdateRequestDto();
        offerTypeUpdateRequestDto.setName(null);
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_OFFER_TYPE_URI)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .content(offerTypeUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS
                        .getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS
                        .getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_ExistingOfferTypeNameIsProvidedForUpdateOfferType() throws Exception {
        OfferTypeUpdateRequestDto offerTypeUpdateRequestDto = getSampleOfferTypeUpdateRequestDto();
        OfferType offerType = getSampleOfferType();
        when(offerTypeService.getOfferTypeById(OFFER_TYPE_ID)).thenReturn(offerType);
        doThrow(new ExistingNameException("ERROR")).when(offerTypeService).updateOfferType(offerType, OFFER_TYPE_NAME);
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_OFFER_TYPE_URI)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .content(offerTypeUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.EXISTING_OFFER_TYPE_NAME
                        .getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.EXISTING_OFFER_TYPE_NAME
                        .getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidOfferTypeIdIsProvidedForUpdateOfferType() throws Exception {
        OfferTypeUpdateRequestDto offerTypeUpdateRequestDto = getSampleOfferTypeUpdateRequestDto();
        OfferType offerType = getSampleOfferType();
        when(offerTypeService.getOfferTypeById(OFFER_TYPE_ID)).thenReturn(offerType);
        doThrow(new InvalidOfferTypeException("ERROR")).when(offerTypeService).updateOfferType(offerType, OFFER_TYPE_NAME);
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_OFFER_TYPE_URI)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .content(offerTypeUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_OFFER_TYPE_ID
                        .getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_OFFER_TYPE_ID
                        .getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_UpdateOfferTypeIsFailed() throws Exception {
        OfferTypeUpdateRequestDto offerTypeUpdateRequestDto = getSampleOfferTypeUpdateRequestDto();
        OfferType offerType = getSampleOfferType();
        when(offerTypeService.getOfferTypeById(OFFER_TYPE_ID)).thenReturn(offerType);
        doThrow(new KupiCoreException("ERROR")).when(offerTypeService).updateOfferType(offerType, OFFER_TYPE_NAME);
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_OFFER_TYPE_URI)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .content(offerTypeUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getCode()));
    }

    @Test
    void Should_ReturnOk_When_GetAllOfferTypesSuccessfully() throws Exception {
        List<OfferType> offerTypeList = getSampleOfferTypeList();
        when(offerTypeService.getAllOfferTypes()).thenReturn(offerTypeList);
        mockMvc.perform(MockMvcRequestBuilders.get(GET_ALL_OFFER_TYPES_URI)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.READ_OFFER_TYPE_LIST.getMessage()))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.READ_OFFER_TYPE_LIST.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_GetAllOfferTypesIsFailed() throws Exception {
        doThrow(new KupiCoreException("ERROR")).when(offerTypeService).getAllOfferTypes();
        mockMvc.perform(MockMvcRequestBuilders.get(GET_ALL_OFFER_TYPES_URI)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getCode()));
    }

    @Test
    void Should_ReturnOk_When_DeleteOfferTypeSuccessfully() throws Exception {
        String uri = DELETE_OFFER_TYPE_URI.replace("{offerTypeId}", OFFER_TYPE_ID);
        mockMvc.perform(MockMvcRequestBuilders.delete(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.DELETE_OFFER_TYPE.getMessage()))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.DELETE_OFFER_TYPE.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_OfferTypeHasRequestADeal() throws Exception {
        doThrow(new UnsupportedDeleteAction("ERROR")).when(offerTypeService).deleteOfferType(OFFER_TYPE_ID);
        String uri = DELETE_OFFER_TYPE_URI.replace("{offerTypeId}", OFFER_TYPE_ID);
        mockMvc.perform(MockMvcRequestBuilders.delete(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.UNSUPPORTED_OFFER_TYPE_DELETE
                        .getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.UNSUPPORTED_OFFER_TYPE_DELETE
                        .getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidOfferTypeIdIsProvidedForDeleteOfferType() throws Exception {
        doThrow(new InvalidOfferTypeException("ERROR")).when(offerTypeService).deleteOfferType(OFFER_TYPE_ID);
        String uri = DELETE_OFFER_TYPE_URI.replace("{offerTypeId}", OFFER_TYPE_ID);
        mockMvc.perform(MockMvcRequestBuilders.delete(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_OFFER_TYPE_ID
                        .getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_OFFER_TYPE_ID
                        .getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_DeleteOfferTypeIsFailed() throws Exception {
        doThrow(new KupiCoreException("ERROR")).when(offerTypeService).deleteOfferType(OFFER_TYPE_ID);
        String uri = DELETE_OFFER_TYPE_URI.replace("{offerTypeId}", OFFER_TYPE_ID);
        mockMvc.perform(MockMvcRequestBuilders.delete(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getCode()));
    }

    /**
     * This method creates sample OfferType
     *
     * @return OfferType
     */
    private OfferType getSampleOfferType() {
        OfferType offerType = new OfferType();
        offerType.setId(OFFER_TYPE_ID);
        offerType.setName(OFFER_TYPE_NAME);
        offerType.setUpdatedAt(System.currentTimeMillis());
        offerType.setCreatedAt(System.currentTimeMillis());
        offerType.setDeleted(false);
        return offerType;
    }

    /**
     * This method creates sample OfferTypeCreateRequestDto
     *
     * @return OfferTypeCreateRequestDto
     */
    private OfferTypeCreateRequestDto getSampleOfferTypeCreateRequestDto() {
        OfferTypeCreateRequestDto offerTypeCreateRequestDto = new OfferTypeCreateRequestDto();
        offerTypeCreateRequestDto.setName(OFFER_TYPE_NAME);
        return offerTypeCreateRequestDto;
    }

    /**
     * This method creates sample OfferTypeUpdateRequestDto
     *
     * @return OfferTypeUpdateRequestDto
     */
    private OfferTypeUpdateRequestDto getSampleOfferTypeUpdateRequestDto() {
        OfferTypeUpdateRequestDto offerTypeUpdateRequestDto = new OfferTypeUpdateRequestDto();
        offerTypeUpdateRequestDto.setId(OFFER_TYPE_ID);
        offerTypeUpdateRequestDto.setName(OFFER_TYPE_NAME);
        return offerTypeUpdateRequestDto;
    }

    /**
     * This method creates sample OfferType list
     *
     * @return offerTypeList
     */
    private List<OfferType> getSampleOfferTypeList() {
        OfferType offerType = getSampleOfferType();
        List<OfferType> offerTypeList = new ArrayList<>();
        offerTypeList.add(offerType);
        return offerTypeList;
    }
}