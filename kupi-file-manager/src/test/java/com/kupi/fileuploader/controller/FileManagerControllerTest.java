package com.kupi.fileuploader.controller;

import com.kupi.fileuploader.configurations.FileUploaderControllerAdvice;
import com.kupi.fileuploader.configurations.ResourceBundleMessageSourceBean;
import com.kupi.fileuploader.configurations.Translator;
import com.kupi.fileuploader.domain.entity.FileManager;
import com.kupi.fileuploader.domain.request.FileUpdateRequestDto;
import com.kupi.fileuploader.domain.response.FileResponseDto;
import com.kupi.fileuploader.domain.response.FileSummaryPageResponse;
import com.kupi.fileuploader.domain.response.FileSummaryResponse;
import com.kupi.fileuploader.domain.response.FileVolumeResponse;
import com.kupi.fileuploader.enums.ErrorResponseStatusType;
import com.kupi.fileuploader.enums.SuccessResponseStatusType;
import com.kupi.fileuploader.exception.FileManagerException;
import com.kupi.fileuploader.exception.InvalidFileException;
import com.kupi.fileuploader.service.FileManagerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This class tests the {@link FileManagerController} class
 */
class FileManagerControllerTest {

    public static final String X_USER_ID = "User-Id";
    private static final String USER_ID = "uid-d9d069f9-2b07-446f-8117-e78ff14d01d8";
    private static final long IMAGE_MAX_BYTE_SIZE = 50;
    private static final String SAMPLE_FILE_URL = "https://media.s3.ap-southeast-1.amazonaws.com/bid-12345.csv";
    private static final String FILE_FORMAT = "text/csv";
    private static final String FILE_NAME = "bid-12345.csv";
    private static final String UPLOAD_FILE_URI = "/api/v1/files/upload";
    private static final String UPDATE_FILE_URI = "/api/v1/files";
    private static final String DOWNLOAD_FILE_URI = "/api/v1/files/download/{fileId}";
    private static final String DELETE_FILE_URI = "/api/v1/files/{fileId}";
    private static final String GET_FILE_DETAIL_URI = "/api/v1/files/{fileId}";
    private static final String GET_FILE_SUMMARY_URI = "/api/v1/files/summary/type/{type}/name/{name}/{page}/{size}";
    private static final String ERROR_STATUS = "ERROR";
    private static final String SUCCESS_STATUS = "SUCCESS";
    private static final String OPTION = "Option";
    private static final String NONE = "NONE";
    private static final int PAGE = 0;
    private static final int SIZE = 1;
    private static final String FILE_ID = "File-Id";
    private static final String NAME = "Name";
    private static final String INTERNAL_SERVER_ERROR = "Failed due to an internal server error";
    private static final int MAX_FILE_COUNT = 10;
    private final ResourceBundleMessageSourceBean resourceBundleMessageSourceBean = new ResourceBundleMessageSourceBean();
    private final List<String> fileTypes = getFileTypes();
    Locale locale = LocaleContextHolder.getLocale();
    private ResourceBundleMessageSource resourceBundleMessageSource;
    private MockMvc mockMvc;
    @Mock
    private FileManagerService fileManagerService;
    @Mock
    private Translator translator;

    @BeforeEach
    void setUp() {
        initMocks(this);
        FileManagerController fileManagerController = new FileManagerController(fileManagerService,
                IMAGE_MAX_BYTE_SIZE, fileTypes, MAX_FILE_COUNT, translator);
        mockMvc = MockMvcBuilders.standaloneSetup(fileManagerController)
                .setControllerAdvice(new FileUploaderControllerAdvice(translator)).build();
        this.resourceBundleMessageSource = resourceBundleMessageSourceBean.messageSource();
    }

    @Test
    void Should_ReturnOk_When_UploadingFileIsSuccess() throws Exception {
        String fileUpload = resourceBundleMessageSource.getMessage(SuccessResponseStatusType.FILE_UPLOAD.getCode(),
                null, locale);
        MockMultipartFile firstFile =
                new MockMultipartFile("files", "filename.pdf",
                        "application/pdf", "some content".getBytes());
        MockMultipartFile secondFile =
                new MockMultipartFile("files", "filename.pdf",
                        "application/pdf", "some content".getBytes());
        when(translator.toLocale(SuccessResponseStatusType.FILE_UPLOAD.getCode()))
                .thenReturn(fileUpload);
        when(fileManagerService.uploadFile(any(MultipartFile.class), eq(FILE_NAME), eq(USER_ID)))
                .thenReturn(mock(FileResponseDto.class));
        mockMvc.perform(MockMvcRequestBuilders.multipart(UPLOAD_FILE_URI)
                        .file(firstFile)
                        .file(secondFile).param("fileName", FILE_NAME)
                        .header(X_USER_ID, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.FILE_UPLOAD.getMessage()))
                .andExpect(jsonPath("$.displayMessage").value(fileUpload));
    }

    @Test
    void Should_ReturnBadRequest_When_FileCountExceedForUploadFile() throws Exception {
        String errorMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType
                        .MAX_FILE_COUNT
                        .getCodeString(ErrorResponseStatusType.MAX_FILE_COUNT.getCode()),
                null, locale);
        when(translator.toLocale(ErrorResponseStatusType.MAX_FILE_COUNT.getCodeString(
                ErrorResponseStatusType.MAX_FILE_COUNT.getCode())))
                .thenReturn(errorMessage);
        MockMultipartFile firstFile = new MockMultipartFile("files", "filename.pdf",
                "application/pdf", "some content".getBytes());
        mockMvc.perform(MockMvcRequestBuilders.multipart(UPLOAD_FILE_URI)
                        .file(firstFile)
                        .file(firstFile)
                        .file(firstFile)
                        .file(firstFile)
                        .file(firstFile)
                        .file(firstFile)
                        .file(firstFile)
                        .file(firstFile)
                        .file(firstFile)
                        .file(firstFile)
                        .file(firstFile).param("fileName", FILE_NAME)
                        .header(X_USER_ID, USER_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.MAX_FILE_COUNT.getMessage()))
                .andExpect(jsonPath("$.displayMessage").value(errorMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UploadingInvalidFileType() throws Exception {
        String errorMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType
                        .UNSUPPORTED_FILE_FORMAT
                        .getCodeString(ErrorResponseStatusType.UNSUPPORTED_FILE_FORMAT.getCode()),
                null, locale);
        when(translator.toLocale(ErrorResponseStatusType.UNSUPPORTED_FILE_FORMAT.getCodeString(
                ErrorResponseStatusType.UNSUPPORTED_FILE_FORMAT.getCode())))
                .thenReturn(errorMessage);
        MockMultipartFile file =
                new MockMultipartFile("files", "filename.pdf", null,
                        "some content".getBytes());
        mockMvc.perform(MockMvcRequestBuilders.multipart(UPLOAD_FILE_URI)
                        .file(file).param("fileName", FILE_NAME)
                        .header(X_USER_ID, USER_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.UNSUPPORTED_FILE_FORMAT.getMessage()))
                .andExpect(jsonPath("$.displayMessage").value(errorMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UploadingLargeFile() throws Exception {
        String errorMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType
                        .EXCEEDED_FILE_SIZE
                        .getCodeString(ErrorResponseStatusType.EXCEEDED_FILE_SIZE.getCode()),
                null, locale);
        when(translator.toLocale(ErrorResponseStatusType.EXCEEDED_FILE_SIZE.getCodeString(
                ErrorResponseStatusType.EXCEEDED_FILE_SIZE.getCode())))
                .thenReturn(errorMessage);
        MockMultipartFile largeFile = new MockMultipartFile("files", FILE_NAME, FILE_FORMAT,
                "1234567890-1234567890-1234567890-1234567890-1234567890".getBytes());
        mockMvc.perform(MockMvcRequestBuilders.multipart(UPLOAD_FILE_URI)
                        .file(largeFile).param("fileName", FILE_NAME)
                        .header(X_USER_ID, USER_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.EXCEEDED_FILE_SIZE.getMessage()))
                .andExpect(jsonPath("$.displayMessage").value(errorMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_UploadingFileIsFailed() throws Exception {
        String errorMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType
                        .INTERNAL_SERVER_ERROR
                        .getCodeString(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()),
                null, locale);
        when(translator.toLocale(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCodeString(
                ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(errorMessage);
        MockMultipartFile file =
                new MockMultipartFile("files", "filename.pdf", "application/pdf",
                        "some content".getBytes());
        doThrow(new FileManagerException("Failed")).when(fileManagerService)
                .uploadFile(any(MultipartFile.class), eq(FILE_NAME), eq(USER_ID));
        mockMvc.perform(MockMvcRequestBuilders.multipart(UPLOAD_FILE_URI)
                        .file(file).param("fileName", FILE_NAME)
                        .header(X_USER_ID, USER_ID))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(INTERNAL_SERVER_ERROR))
                .andExpect(jsonPath("$.displayMessage").value(errorMessage));
    }

    @Test
    void Should_ReturnOk_When_TypeAndNameAreNoneForGetFileSummary() throws Exception {
        Pageable pageable = getSamplePageable();
        FileSummaryPageResponse fileSummaryPageResponse = getSampleFileSummaryPageResponse();
        when(fileManagerService.getAllFileSummary(USER_ID, pageable)).thenReturn(fileSummaryPageResponse);
        String uri = GET_FILE_SUMMARY_URI.replace("{type}", NONE).replace("{name}", NONE)
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE));
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(X_USER_ID, USER_ID)
                        .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.FILE_SUMMARY.getMessage()));
    }

    @Test
    void Should_ReturnOk_When_TypeAndNameAreProvidedForGetFileSummary() throws Exception {
        Pageable pageable = getSamplePageable();
        FileSummaryPageResponse fileSummaryPageResponse = getSampleFileSummaryPageResponse();
        when(fileManagerService.getFileSummaryByNameOrType(USER_ID, OPTION, NAME, pageable))
                .thenReturn(fileSummaryPageResponse);
        String uri = GET_FILE_SUMMARY_URI.replace("{type}", OPTION).replace("{name}", NAME)
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE));
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(X_USER_ID, USER_ID)
                        .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.FILE_SUMMARY.getMessage()));
    }

    @Test
    void Should_ReturnInternalServerError_When_GetFileSummaryIsFailed() throws Exception {
        Pageable pageable = getSamplePageable();
        doThrow(new FileManagerException("ERROR")).when(fileManagerService)
                .getFileSummaryByNameOrType(USER_ID, OPTION, NAME, pageable);
        String uri = GET_FILE_SUMMARY_URI.replace("{type}", OPTION).replace("{name}", NAME)
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE));
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(X_USER_ID, USER_ID)
                        .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()));
    }

    @Test
    void Should_ReturnOk_When_FileUpdatedSuccessfully() throws Exception {
        FileUpdateRequestDto fileUpdateRequestDto = getSampleFileUpdateRequestDto();
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_FILE_URI)
                        .header(X_USER_ID, USER_ID)
                        .content(fileUpdateRequestDto.toJson())
                        .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.UPDATE_FILE.getMessage()));
    }

    @Test
    void Should_ReturnBadRequest_When_MissingRequiredFieldsAreProvidedForUpdateFile() throws Exception {
        FileUpdateRequestDto fileUpdateRequestDto = getSampleFileUpdateRequestDto();
        fileUpdateRequestDto.setFileId("");
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_FILE_URI)
                        .header(X_USER_ID, USER_ID)
                        .content(fileUpdateRequestDto.toJson())
                        .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getMessage()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidFileIdIsProvidedForUpdateFile() throws Exception {
        FileUpdateRequestDto fileUpdateRequestDto = getSampleFileUpdateRequestDto();
        doThrow(new InvalidFileException("ERROR")).when(fileManagerService)
                .updateFile(any(FileUpdateRequestDto.class), anyString());
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_FILE_URI)
                        .header(X_USER_ID, USER_ID)
                        .content(fileUpdateRequestDto.toJson())
                        .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_FILE_ID.getMessage()));
    }

    @Test
    void Should_ReturnInternalServerError_When_UpdateFileIsFailed() throws Exception {
        FileUpdateRequestDto fileUpdateRequestDto = getSampleFileUpdateRequestDto();
        doThrow(new FileManagerException("ERROR")).when(fileManagerService)
                .updateFile(any(FileUpdateRequestDto.class), anyString());
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_FILE_URI)
                        .header(X_USER_ID, USER_ID)
                        .content(fileUpdateRequestDto.toJson())
                        .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()));
    }

    @Test
    void Should_ReturnOk_When_DeleteFileSuccessfully() throws Exception {
        String uri = DELETE_FILE_URI.replace("{fileId}", FILE_ID);
        mockMvc.perform(MockMvcRequestBuilders.delete(uri)
                        .header(X_USER_ID, USER_ID)
                        .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.DELETE_FILE.getMessage()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidFileIdIsProvidedForDeleteFile() throws Exception {
        doThrow(new InvalidFileException("ERROR")).when(fileManagerService).deleteFile(FILE_ID, USER_ID);
        String uri = DELETE_FILE_URI.replace("{fileId}", FILE_ID);
        mockMvc.perform(MockMvcRequestBuilders.delete(uri)
                        .header(X_USER_ID, USER_ID)
                        .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_FILE_ID.getMessage()));
    }

    @Test
    void Should_ReturnInternalServerError_When_DeleteFileIsFailed() throws Exception {
        doThrow(new FileManagerException("ERROR")).when(fileManagerService).deleteFile(FILE_ID, USER_ID);
        String uri = DELETE_FILE_URI.replace("{fileId}", FILE_ID);
        mockMvc.perform(MockMvcRequestBuilders.delete(uri)
                        .header(X_USER_ID, USER_ID)
                        .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()));
    }

    @Test
    void Should_ReturnOk_When_GetFileDetailSuccessfully() throws Exception {
        FileManager fileManager = getSampleFileManager();
        String uri = GET_FILE_DETAIL_URI.replace("{fileId}", FILE_ID);
        when(fileManagerService.getFileDetail(FILE_ID, USER_ID)).thenReturn(fileManager);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(X_USER_ID, USER_ID)
                        .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.FILE_DETAIL.getMessage()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidFileIdIsProvidedForGetFileDetail() throws Exception {
        doThrow(new InvalidFileException("ERROR")).when(fileManagerService).getFileDetail(anyString(), anyString());
        String uri = GET_FILE_DETAIL_URI.replace("{fileId}", FILE_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(X_USER_ID, USER_ID)
                        .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_FILE_ID.getMessage()));
    }

    @Test
    void Should_ReturnInternalServerError_When_GetFileDetailIsFailed() throws Exception {
        doThrow(new FileManagerException("ERROR")).when(fileManagerService).getFileDetail(anyString(), anyString());
        String uri = GET_FILE_DETAIL_URI.replace("{fileId}", FILE_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(X_USER_ID, USER_ID)
                        .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()));
    }

    @Test
    void Should_ReturnOk_When_DownloadFileSuccessfully() throws Exception {
        String uri = DOWNLOAD_FILE_URI.replace("{fileId}", FILE_ID);
        byte[] bytes = new byte[100000];
        when(fileManagerService.getFileNameWithExtension(FILE_ID, USER_ID)).thenReturn(FILE_NAME);
        when(fileManagerService.downloadFile(FILE_NAME)).thenReturn(bytes);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(X_USER_ID, USER_ID)
                        .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/octet-stream"));
    }

    @Test
    void Should_ReturnBadRequest_When_FileIsInvalidIsDownloaded() throws Exception {
        String uri = DOWNLOAD_FILE_URI.replace("{fileId}", FILE_ID);
        when(fileManagerService.getFileNameWithExtension(FILE_ID, USER_ID)).thenReturn(FILE_NAME);
        doThrow(new InvalidFileException("ERROR")).when(fileManagerService).downloadFile(FILE_NAME);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(X_USER_ID, USER_ID)
                        .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void Should_ReturnBadRequest_When_DownloadFileIsFailed() throws Exception {
        String uri = DOWNLOAD_FILE_URI.replace("{fileId}", FILE_ID);
        byte[] bytes = new byte[100000];
        when(fileManagerService.getFileNameWithExtension(FILE_ID, USER_ID)).thenReturn(FILE_NAME);
        doThrow(new FileManagerException("ERROR")).when(fileManagerService).downloadFile(FILE_NAME);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(X_USER_ID, USER_ID)
                        .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    /**
     * This method gives sample file types
     *
     * @return FileTypes
     */
    private List<String> getFileTypes() {
        List<String> fileTypes = new ArrayList<>();
        fileTypes.add(0, "text/csv");
        fileTypes.add(1, "application/pdf");
        fileTypes.add(2, "audio/mpeg");
        fileTypes.add(3, "video/mp4");
        return fileTypes;
    }

    /**
     * This method creates sample FileSummaryPageResponse
     *
     * @return FileSummaryPageResponse
     */
    private FileSummaryPageResponse getSampleFileSummaryPageResponse() {
        FileManager fileManager = getSampleFileManager();
        List<FileManager> fileManagerList = new ArrayList<>();
        fileManagerList.add(fileManager);
        Page<FileManager> fileManagerPage = getSampleFileManagerPage();
        FileVolumeResponse fileVolumeResponse = new FileVolumeResponse(10, 5);
        FileSummaryResponse fileSummaryResponse = new FileSummaryResponse(fileVolumeResponse, getFileTypes());
        return new FileSummaryPageResponse(fileManagerPage, fileSummaryResponse, fileManagerList);
    }

    /**
     * This method creates sample FileManager page
     *
     * @return FileManagerPage
     */
    private Page<FileManager> getSampleFileManagerPage() {
        FileManager fileManager = getSampleFileManager();
        List<FileManager> fileManagerList = new ArrayList<>();
        fileManagerList.add(fileManager);
        return new PageImpl<>(fileManagerList, getSamplePageable(), 1);
    }

    /**
     * This method creates sample FileManager
     *
     * @return FileManager
     */
    private FileManager getSampleFileManager() {
        FileManager fileManager = new FileManager();
        fileManager.setId("ID");
        fileManager.setName("File Name");
        fileManager.setContentType("application/pdf");
        fileManager.setUrl(SAMPLE_FILE_URL);
        return fileManager;
    }

    /**
     * This method creates sample Pageable
     *
     * @return Pageable
     */
    private Pageable getSamplePageable() {
        return PageRequest.of(PAGE, SIZE);
    }

    /**
     * This method creates sample FileUpdateRequestDto
     *
     * @return FileUpdateRequestDto
     */
    private FileUpdateRequestDto getSampleFileUpdateRequestDto() {
        FileUpdateRequestDto fileUpdateRequestDto = new FileUpdateRequestDto();
        fileUpdateRequestDto.setFileId("ID");
        fileUpdateRequestDto.setDescription("Description");
        fileUpdateRequestDto.setName("Name");
        return fileUpdateRequestDto;
    }
}