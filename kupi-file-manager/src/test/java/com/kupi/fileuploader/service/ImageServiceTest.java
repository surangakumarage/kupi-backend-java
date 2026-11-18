package com.kupi.fileuploader.service;

import com.kupi.fileuploader.exception.InvalidFileException;
import com.kupi.fileuploader.exception.FileManagerException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * This class tests the {@link ImageService} class.
 */
class ImageServiceTest {

    private static final String BUCKET_NAME = "media";
    private static final String REGION = "ap-southeast-1";
    private static final String SAMPLE_IMAGE_URL = "https://media.s3.ap-southeast-1.amazonaws.com/iid-b94ce7c3-ed51-4a5e-97a4-59824baf286c";
    private static final String IMAGE_PNG = "image/png";
    private static final String IMAGE_NAME = "iid-b94ce7c3-ed51-4a5e-97a4-59824baf286c";
    private static final String IMAGE_UPLOAD_ERROR = "Uploading image to S3 bucket was filed";
    private static final String IMAGE_DELETE_ERROR = "Deleting image from S3 bucket was filed";
    private static final String INVALID_IMAGE_URL = "Invalid image url";
    private ImageService imageService;
    @Mock
    private S3Client s3Client;
    private MockMultipartFile image;

    @BeforeEach
    void setUp() {
        initMocks(this);
        imageService = new ImageService(s3Client, BUCKET_NAME, REGION);
        image = new MockMultipartFile("IMAGE_UPLOAD_PARAM_NAME", "test.png",
                IMAGE_PNG, "1234567890".getBytes());
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_UploadImage_When_UploadNameIsEmpty() {
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(PutObjectResponse.builder().build());
        String imageUrl = imageService.uploadImage(image, "");
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        assertEquals(SAMPLE_IMAGE_URL, imageUrl);
    }

    @Test
    void Should_UploadImage_When_UploadNameIsNull() {
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(PutObjectResponse.builder().build());
        String imageUrl = imageService.uploadImage(image, null);
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        assertEquals(SAMPLE_IMAGE_URL, imageUrl);
    }

    @Test
    void Should_UploadImage_When_UploadNameIsGiven() {
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(PutObjectResponse.builder().build());
        String imageUrl = imageService.uploadImage(image, "upload-name");
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        assertEquals(SAMPLE_IMAGE_URL, imageUrl);
    }

    @Test
    void Should_ThrowException_When_UploadingImageWasFailedDueToAwsClientError() {
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(SdkException.builder().message("failed").build());
        FileManagerException exception = assertThrows(
                FileManagerException.class, () -> imageService.uploadImage(image, null));
        assertEquals(IMAGE_UPLOAD_ERROR, exception.getMessage());
    }

    @Test
    void Should_ThrowException_When_UploadingImageWasFailedDueToIOError() throws IOException {
        MultipartFile image = mock(MultipartFile.class);
        when(image.getInputStream()).thenThrow(new IOException("failed"));
        when(image.getContentType()).thenReturn(IMAGE_PNG);
        when(image.getSize()).thenReturn(10L);
        FileManagerException exception = assertThrows(
                FileManagerException.class, () -> imageService.uploadImage(image, null));
        assertEquals(IMAGE_UPLOAD_ERROR, exception.getMessage());
    }

    @Test
    void Should_DeleteImage() {
        when(s3Client.deleteObject(any(DeleteObjectRequest.class))).thenReturn(DeleteObjectResponse.builder().build());
        imageService.deleteImage(SAMPLE_IMAGE_URL);
        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void Should_ThrowException_When_DeletingImageWasFailedDueToAwsClientError() {
        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .thenThrow(SdkException.builder().message("failed").build());
        FileManagerException exception = assertThrows(
                FileManagerException.class, () -> imageService.deleteImage(SAMPLE_IMAGE_URL));
        assertEquals(IMAGE_DELETE_ERROR, exception.getMessage());
    }

    @Test
    void Should_ThrowException_When_DeletingInvalidImageUrl() {
        InvalidFileException exception = assertThrows(
                InvalidFileException.class, () -> imageService.deleteImage("https://image"));
        assertEquals(INVALID_IMAGE_URL, exception.getMessage());
    }

}