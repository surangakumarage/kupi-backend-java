package com.kupi.fileuploader.controller;

import com.kupi.fileuploader.configurations.FileUploaderControllerAdvice;
import com.kupi.fileuploader.configurations.ResourceBundleMessageSourceBean;
import com.kupi.fileuploader.configurations.Translator;
import com.kupi.fileuploader.domain.response.CsvS3UploadResponseDto;
import com.kupi.fileuploader.enums.ErrorResponseStatusType;
import com.kupi.fileuploader.enums.SuccessResponseStatusType;
import com.kupi.fileuploader.exception.FileManagerException;
import com.kupi.fileuploader.service.CsvService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * This class tests {@link CsvFileController} class
 */
class CsvFileControllerTest {

    private static final String UPLOAD_FILE_NAME = "file";
    private static final String UPLOAD_FILE_ORIGINAL_NAME = "file.csv";
    private static final String UPLOAD_FILE_CONTENT = "Welcome!";
    private static final String UPLOAD_FILE_TYPE = "text/csv";
    private static final String S3_UPLOAD_FILE_URL = "FILE_URL";
    private static final byte[] UPLOAD_FILE_CONTENT_SIZE = UPLOAD_FILE_CONTENT.getBytes();
    private static final String SUCCESS_STATUS = "SUCCESS";
    private static final String ERROR_STATUS = "ERROR";
    private static final String FAILED = "failed";
    private static final String CSV_FILE_UPLOAD_URI = "/api/v1/csv/upload";

    private final ResourceBundleMessageSourceBean resourceBundleMessageSourceBean = new ResourceBundleMessageSourceBean();
    Locale locale = LocaleContextHolder.getLocale();
    @Mock
    private Translator translator;
    @Mock
    private CsvService csvService;
    private MockMvc mockMvc;
    private ResourceBundleMessageSource resourceBundleMessageSource;

    @BeforeEach
    void setUp() {
        initMocks(this);
        CsvFileController csvFileController = new CsvFileController(10, translator, csvService);
        mockMvc = MockMvcBuilders.standaloneSetup(csvFileController)
                .setControllerAdvice(new FileUploaderControllerAdvice(translator))
                .build();
        resourceBundleMessageSource = resourceBundleMessageSourceBean.messageSource();
    }

    @AfterEach
    void tearDown() {
    }

    /**
     * Start of tests for upload csv file
     * Api context: /api/v1/csv/upload
     */
    @Test
    void Should_ReturnOk_When_UploadingCSVFileIsSuccessful() throws Exception {
        when(csvService.uploadCsv(any(MultipartFile.class), anyString())).thenReturn(getSampleCsvS3UploadResponseDto());

        String successMessage = resourceBundleMessageSource.getMessage(SuccessResponseStatusType.CSV_UPLOAD.getCode(),
                null, locale);
        when(translator.toLocale(SuccessResponseStatusType.CSV_UPLOAD.getCode())).thenReturn(successMessage);
        MockMultipartFile mockFile = new MockMultipartFile(UPLOAD_FILE_NAME, UPLOAD_FILE_ORIGINAL_NAME,
                UPLOAD_FILE_TYPE, UPLOAD_FILE_CONTENT_SIZE);
        mockMvc.perform(MockMvcRequestBuilders.multipart(CSV_FILE_UPLOAD_URI).file(mockFile)
                        .param("fileName", "fileName")
                        .contentType(MediaType.MULTIPART_FORM_DATA).accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.CSV_UPLOAD.getMessage()))
                .andExpect(jsonPath("$.data.contentType").value(UPLOAD_FILE_TYPE))
                .andExpect(jsonPath("$.data.csvByteSize").value(UPLOAD_FILE_CONTENT_SIZE.length))
                .andExpect(jsonPath("$.data.csvUrl").value(S3_UPLOAD_FILE_URL))
                .andExpect(jsonPath("$.data.fileName").value(UPLOAD_FILE_NAME))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UploadingCSVFileForMissingRequiredFields() throws Exception {
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .MISSING_REQUIRED_FIELDS.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode())))
                .thenReturn(invalidMessage);
        MockMultipartFile mockFile = new MockMultipartFile(UPLOAD_FILE_NAME, UPLOAD_FILE_ORIGINAL_NAME,
                UPLOAD_FILE_TYPE, new byte[0]);
        mockMvc.perform(MockMvcRequestBuilders.multipart(CSV_FILE_UPLOAD_URI).file(mockFile)
                        .param("fileName", "fileName")
                        .contentType(MediaType.MULTIPART_FORM_DATA).accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UploadingCSVFileForUnsupportedFileFormat() throws Exception {
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .UNSUPPORTED_FILE_FORMAT.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.UNSUPPORTED_FILE_FORMAT.getCode())))
                .thenReturn(invalidMessage);
        MockMultipartFile mockFile = new MockMultipartFile(UPLOAD_FILE_NAME, "file.pdf",
                UPLOAD_FILE_TYPE, UPLOAD_FILE_CONTENT_SIZE);
        mockMvc.perform(MockMvcRequestBuilders.multipart(CSV_FILE_UPLOAD_URI).file(mockFile)
                        .param("fileName", "fileName")
                        .contentType(MediaType.MULTIPART_FORM_DATA).accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.UNSUPPORTED_FILE_FORMAT
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.UNSUPPORTED_FILE_FORMAT
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UploadingCSVFileForExceededCSVSize() throws Exception {
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .EXCEEDED_CVS_SIZE.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.EXCEEDED_CVS_SIZE.getCode())))
                .thenReturn(invalidMessage);
        MockMultipartFile mockFile = new MockMultipartFile(UPLOAD_FILE_NAME, UPLOAD_FILE_ORIGINAL_NAME,
                UPLOAD_FILE_TYPE, new byte[100]);
        mockMvc.perform(MockMvcRequestBuilders.multipart(CSV_FILE_UPLOAD_URI).file(mockFile)
                        .param("fileName", "fileName")
                        .contentType(MediaType.MULTIPART_FORM_DATA).accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.EXCEEDED_CVS_SIZE
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.EXCEEDED_CVS_SIZE
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UploadingCSVFileForInvalidCSVType() throws Exception {
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_CSV_TYPE.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_CSV_TYPE.getCode())))
                .thenReturn(invalidMessage);
        MockMultipartFile mockFile = new MockMultipartFile(UPLOAD_FILE_NAME, UPLOAD_FILE_ORIGINAL_NAME,
                null, UPLOAD_FILE_CONTENT_SIZE);
        mockMvc.perform(MockMvcRequestBuilders.multipart(CSV_FILE_UPLOAD_URI).file(mockFile)
                        .param("fileName", "fileName")
                        .contentType(MediaType.MULTIPART_FORM_DATA).accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_CSV_TYPE
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_CSV_TYPE
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_UploadingCSVFileIsFailed() throws Exception {
        when(csvService.uploadCsv(any(MultipartFile.class), anyString())).thenThrow(new FileManagerException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);
        MockMultipartFile mockFile = new MockMultipartFile(UPLOAD_FILE_NAME, UPLOAD_FILE_ORIGINAL_NAME,
                UPLOAD_FILE_TYPE, UPLOAD_FILE_CONTENT_SIZE);
        mockMvc.perform(MockMvcRequestBuilders.multipart(CSV_FILE_UPLOAD_URI).file(mockFile)
                        .param("fileName", "fileName")
                        .contentType(MediaType.MULTIPART_FORM_DATA).accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    /**
     * This method returns a sample Csv S3 Upload Response Dto
     *
     * @return CsvS3UploadResponseDto
     */
    private CsvS3UploadResponseDto getSampleCsvS3UploadResponseDto() {
        CsvS3UploadResponseDto csvS3UploadResponseDto = new CsvS3UploadResponseDto();
        csvS3UploadResponseDto.setFileUrl(S3_UPLOAD_FILE_URL);
        csvS3UploadResponseDto.setFileName(UPLOAD_FILE_NAME);
        return csvS3UploadResponseDto;
    }
}