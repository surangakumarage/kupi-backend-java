package com.kupi.fileuploader.service;

import com.kupi.fileuploader.exception.FileManagerException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * This class tests {@link CsvService} class
 */
class CsvServiceTest {

    private static final String UPLOAD_FILE_NAME = "file";
    private static final String UPLOAD_FILE_ORIGINAL_NAME = "file.csv";
    private static final String UPLOAD_FILE_CONTENT = "Welcome!";
    private static final String UPLOAD_FILE_TYPE = "text/csv";
    private static final byte[] UPLOAD_FILE_CONTENT_SIZE = UPLOAD_FILE_CONTENT.getBytes();
    private static final String ERROR = "ERROR";
    private static final String UPLOAD_FILE_URL = "https://www.sample-domain.com/";
    private static final String STORAGE_PROVIDER = "s3";
    private static final String AWS_REGION = "ap-southeast-1";

    @Mock
    private S3Client s3Client;
    private CsvService csvService;
    private MockMultipartFile multipartFile;

    @BeforeEach
    void setUp() {
        initMocks(this);
        csvService = new CsvService(s3Client, "objects-kupi-dev", "/coupon_uploads", STORAGE_PROVIDER, AWS_REGION);
        multipartFile = new MockMultipartFile(UPLOAD_FILE_NAME, UPLOAD_FILE_ORIGINAL_NAME, UPLOAD_FILE_TYPE,
                UPLOAD_FILE_CONTENT_SIZE);
    }

    @AfterEach
    void tearDown() {
    }

    /**
     * Upload CSV
     */
    @Test
    void Should_ReturnCsvS3UploadResponseDto_When_UploadingCSVIsSuccessful() throws MalformedURLException {
        // Mock the putObject call - returns PutObjectResponse
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(PutObjectResponse.builder().build());
        assertEquals(UPLOAD_FILE_URL, csvService.uploadCsv(multipartFile, "fileName").getFileUrl());
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void Should_ThrowFileManagerException_When_UploadingCSVIsFailed() {
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(SdkException.builder().message(ERROR).build());
        FileManagerException exception = assertThrows(FileManagerException.class, () -> csvService
                .uploadCsv(multipartFile, "fileName"));
        assertEquals("Uploading csv to S3 bucket was filed", exception.getMessage());
    }

    private URL getSampleUrl() throws MalformedURLException {
        return new URL(UPLOAD_FILE_URL);
    }
}