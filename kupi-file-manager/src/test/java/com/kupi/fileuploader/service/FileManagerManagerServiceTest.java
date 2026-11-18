package com.kupi.fileuploader.service;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import com.kupi.fileuploader.domain.entity.FileManager;
import com.kupi.fileuploader.domain.request.FileUpdateRequestDto;
import com.kupi.fileuploader.domain.response.FileResponseDto;
import com.kupi.fileuploader.domain.response.FileSummaryPageResponse;
import com.kupi.fileuploader.domain.response.FileSummaryResponse;
import com.kupi.fileuploader.domain.response.FileVolumeResponse;
import com.kupi.fileuploader.exception.FileManagerException;
import com.kupi.fileuploader.exception.InvalidFileException;
import com.kupi.fileuploader.repository.FileManagerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * This class tests the {@link FileManagerManagerServiceTest} class.
 */
class FileManagerManagerServiceTest {

    private static final String BUCKET_NAME = "media";
    private static final String SAMPLE_FILE_URL = "https://media.s3.ap-southeast-1.amazonaws.com/bid-12345.csv";
    private static final String FILE_FORMAT = "text/csv";
    private static final String FILE_NAME = "bid-12345.csv";
    private static final String FILE_UPLOAD_ERROR = "Uploading file to S3 bucket was filed";
    private static final String FILE_DOWNLOAD_ERROR = "Error occurred while downloading the file";
    private static final String FILE_NOT_FOUND = "File does not exists in s3";
    private static final String USER_ID = "uid-123";
    private static final String OPTION = "Option";
    private static final String NONE = "NONE";
    private static final String FILE_ID = "File-Id";
    private static final String NAME = "Name";
    private static final long TOTAL_SIZE = 10000;
    private static final long MAX_BYTE_SIZE = 10000000;
    private FileManagerService fileManagerService;
    @Mock
    private FileManagerRepository fileManagerRepository;
    @Mock
    private S3Client s3Client;
    private static final String STORAGE_PROVIDER = "AWS";

    @BeforeEach
    void setUp() {
        initMocks(this);
        fileManagerService = new FileManagerService(s3Client, BUCKET_NAME, fileManagerRepository, MAX_BYTE_SIZE, STORAGE_PROVIDER, "ap-southeast-1");
    }

    @Test
    void Should_ReturnFileResponseDto_When_UploadFileIsSuccessfully() {
        MultipartFile file = new MockMultipartFile("files", "filename.pdf",
                "application/pdf", "some content".getBytes());
        FileResponseDto fileResponseDto = new FileResponseDto(file, SAMPLE_FILE_URL, FILE_NAME);
        assertEquals(fileResponseDto.getName(), fileManagerService.uploadFile(file, FILE_NAME, USER_ID).getName());
    }

    @Test
    void Should_ThrowFileManagerException_When_SaveFileMetaData() {
        MultipartFile file = new MockMultipartFile("files", "filename.pdf",
                "application/pdf", "some content".getBytes());
        when(fileManagerRepository.save(any(FileManager.class))).thenThrow(new DataAccessException("ERROR") {
        });
        FileManagerException fileManagerException = assertThrows(FileManagerException.class, () ->
                fileManagerService.uploadFile(file, FILE_NAME, USER_ID));
        assertEquals("Error while saving file metadata", fileManagerException.getMessage());
    }

    @Test
    void Should_ThrowException_When_UploadingImageWasFailedDueToIOError() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenThrow(new IOException("failed"));
        FileManagerException exception = assertThrows(
                FileManagerException.class, () -> fileManagerService.uploadFile(file, null, USER_ID));
        assertEquals(FILE_UPLOAD_ERROR, exception.getMessage());
    }

    @Test
    void Should_DownloadFile_When_FileNameIsGiven() throws Exception {
        String s = "12345678";
        InputStream inputStream = new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
        ResponseInputStream<GetObjectResponse> responseInputStream = mock(ResponseInputStream.class);
        when(responseInputStream.readAllBytes()).thenReturn(s.getBytes(StandardCharsets.UTF_8));
        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(responseInputStream);
        byte[] bytes = fileManagerService.downloadFile(FILE_NAME);
        verify(s3Client).getObject(any(GetObjectRequest.class));
        assertNotNull(bytes);
    }

    @Test
    void Should_ThrowException_When_FileNotFound() {
        when(s3Client.getObject(any(GetObjectRequest.class))).thenThrow(NoSuchKeyException.builder().message("failed").build());
        NoSuchFileException exception = assertThrows(
                NoSuchFileException.class, () -> fileManagerService.downloadFile(FILE_NAME));
        assertEquals(FILE_NOT_FOUND, exception.getMessage());
    }

    @Test
    void Should_ThrowException_When_FileNotFoundOrErrorWhileDownloading() {
        when(s3Client.getObject(any(GetObjectRequest.class))).thenThrow(SdkException.builder().message("failed").build());
        FileManagerException exception = assertThrows(
                FileManagerException.class, () -> fileManagerService.downloadFile(FILE_NAME));
        assertEquals(FILE_DOWNLOAD_ERROR, exception.getMessage());
    }

    @Test
    void Should_ThrowException_When_FailedToDownload() {
        when(s3Client.getObject(any(GetObjectRequest.class))).thenThrow(SdkException.builder().message("failed").build());
        FileManagerException exception = assertThrows(
                FileManagerException.class, () -> fileManagerService.downloadFile(FILE_NAME));
        assertEquals(FILE_DOWNLOAD_ERROR, exception.getMessage());
    }

    @Test
    void Should_ReturnFileSummaryPage_When_ValidUserIdIsProvided() {
        Pageable pageable = getSamplePageable();
        Page<FileManager> fileManagerPage = getSampleFileManagerPage();
        FileSummaryPageResponse fileSummaryPageResponse = getSampleFileSummaryPageResponse();
        when(fileManagerRepository.findByUserIdOrderByCreatedAtDesc(USER_ID, pageable)).thenReturn(fileManagerPage);
        when(fileManagerRepository.calculateTotalSize(USER_ID)).thenReturn(TOTAL_SIZE);
        assertEquals(fileSummaryPageResponse.getFiles(),
                fileManagerService.getAllFileSummary(USER_ID, pageable).getFiles());
    }

    @Test
    void Should_ThrowFileManagerException_When_GetFileManagerPageFromDataBaseIsFailed() {
        Pageable pageable = getSamplePageable();
        when(fileManagerRepository.findByUserIdOrderByCreatedAtDesc(USER_ID, pageable))
                .thenThrow(new DataAccessException("ERROR") {
                });
        FileManagerException fileManagerException = assertThrows(FileManagerException.class, () ->
                fileManagerService.getAllFileSummary(USER_ID, pageable));
        assertEquals("Error while getting file details", fileManagerException.getMessage());
    }

    @Test
    void Should_ThrowFileManagerException_When_GetRemainingFileSizeFromDataBaseIsFailed() {
        Pageable pageable = getSamplePageable();
        Page<FileManager> fileManagerPage = getSampleFileManagerPage();
        when(fileManagerRepository.findByUserIdOrderByCreatedAtDesc(USER_ID, pageable)).thenReturn(fileManagerPage);
        when(fileManagerRepository.calculateTotalSize(USER_ID))
                .thenThrow(new DataAccessException("ERROR") {
                });
        FileManagerException fileManagerException = assertThrows(FileManagerException.class, () ->
                fileManagerService.getAllFileSummary(USER_ID, pageable));
        assertEquals("Error while getting total file size", fileManagerException.getMessage());
    }

    @Test
    void Should_ReturnFileSummaryPage_When_NameIsNoneProvidedForGetFileSummaryByNameOrType() {
        Pageable pageable = getSamplePageable();
        Page<FileManager> fileManagerPage = getSampleFileManagerPage();
        FileSummaryPageResponse fileSummaryPageResponse = getSampleFileSummaryPageResponse();
        when(fileManagerRepository.findByUserIdAndFileType(USER_ID, OPTION, "", pageable))
                .thenReturn(fileManagerPage);
        when(fileManagerRepository.calculateTotalSize(USER_ID)).thenReturn(TOTAL_SIZE);
        assertEquals(fileSummaryPageResponse.getFiles(),
                fileManagerService.getFileSummaryByNameOrType(USER_ID, OPTION, NONE, pageable).getFiles());
    }

    @Test
    void Should_ReturnFileSummaryPage_When_OptionIsNoneProvidedForGetFileSummaryByNameOrType() {
        Pageable pageable = getSamplePageable();
        Page<FileManager> fileManagerPage = getSampleFileManagerPage();
        FileSummaryPageResponse fileSummaryPageResponse = getSampleFileSummaryPageResponse();
        when(fileManagerRepository.findByUserIdAndFileType(USER_ID, "", NAME, pageable))
                .thenReturn(fileManagerPage);
        when(fileManagerRepository.calculateTotalSize(USER_ID)).thenReturn(TOTAL_SIZE);
        assertEquals(fileSummaryPageResponse.getFiles(),
                fileManagerService.getFileSummaryByNameOrType(USER_ID, NONE, NAME, pageable).getFiles());
    }

    @Test
    void Should_ReturnFileSummaryPage_When_BothOptionAndNameAreProvidedForGetFileSummaryByNameOrType() {
        Pageable pageable = getSamplePageable();
        Page<FileManager> fileManagerPage = getSampleFileManagerPage();
        FileSummaryPageResponse fileSummaryPageResponse = getSampleFileSummaryPageResponse();
        when(fileManagerRepository.findByUserIdAndFileType(USER_ID, OPTION, NAME, pageable))
                .thenReturn(fileManagerPage);
        when(fileManagerRepository.calculateTotalSize(USER_ID)).thenReturn(TOTAL_SIZE);
        assertEquals(fileSummaryPageResponse.getFiles(),
                fileManagerService.getFileSummaryByNameOrType(USER_ID, OPTION, NAME, pageable).getFiles());
    }

    @Test
    void Should_ThrowFileManagerException_When_GetFileManagerPageFromDataBaseIsFailedForGetFileSummaryByNameOrType() {
        Pageable pageable = getSamplePageable();
        when(fileManagerRepository.findByUserIdAndFileType(USER_ID, OPTION, NAME, pageable))
                .thenThrow(new DataAccessException("ERROR") {
                });
        FileManagerException fileManagerException = assertThrows(FileManagerException.class, () ->
                fileManagerService.getFileSummaryByNameOrType(USER_ID, OPTION, NAME, pageable));
        assertEquals("Error while getting file details", fileManagerException.getMessage());
    }

    @Test
    void Should_ThrowFileManagerException_When_GetRemainingFileSizeFromDataBaseIsFailedForGetFileSummaryByNameOrType() {
        Pageable pageable = getSamplePageable();
        Page<FileManager> fileManagerPage = getSampleFileManagerPage();
        when(fileManagerRepository.findByUserIdAndFileType(USER_ID, OPTION, NAME, pageable)).thenReturn(fileManagerPage);
        when(fileManagerRepository.calculateTotalSize(USER_ID))
                .thenThrow(new DataAccessException("ERROR") {
                });
        FileManagerException fileManagerException = assertThrows(FileManagerException.class, () ->
                fileManagerService.getFileSummaryByNameOrType(USER_ID, OPTION, NAME, pageable));
        assertEquals("Error while getting total file size", fileManagerException.getMessage());
    }

    @Test
    void Should_UpdateFile() {
        FileManager fileManager = getSampleFileManager();
        FileUpdateRequestDto fileUpdateRequestDto = getSampleFileUpdateRequestDto();
        when(fileManagerRepository.findByIdAndUserId(FILE_ID, USER_ID)).thenReturn(Optional.of(fileManager));
        fileManagerService.updateFile(fileUpdateRequestDto, USER_ID);
        verify(fileManagerRepository).save(fileManager);
    }

    @Test
    void Should_ThrowInvalidFileException_When_InvalidFileIdIsProvidedForUpdateFile() {
        FileUpdateRequestDto fileUpdateRequestDto = getSampleFileUpdateRequestDto();
        when(fileManagerRepository.findByIdAndUserId(FILE_ID, USER_ID)).thenReturn(Optional.empty());
        InvalidFileException invalidFileException = assertThrows(InvalidFileException.class, () ->
                fileManagerService.updateFile(fileUpdateRequestDto, USER_ID));
        assertEquals("File not found", invalidFileException.getMessage());
    }

    @Test
    void Should_ThrowFileManagerException_When_FileManagerNotUpdatedIntoDataBase() {
        FileManager fileManager = getSampleFileManager();
        FileUpdateRequestDto fileUpdateRequestDto = getSampleFileUpdateRequestDto();
        when(fileManagerRepository.findByIdAndUserId(FILE_ID, USER_ID)).thenReturn(Optional.of(fileManager));
        when(fileManagerRepository.save(fileManager)).thenThrow(new DataAccessException("ERROR") {
        });
        FileManagerException fileManagerException = assertThrows(FileManagerException.class, () ->
                fileManagerService.updateFile(fileUpdateRequestDto, USER_ID));
        assertEquals("Error while updating the file details ", fileManagerException.getMessage());
    }

    @Test
    void Should_DeleteFile() {
        FileManager fileManager = getSampleFileManager();
        String fileNameWithExtension = fileManager.getUrl().substring(fileManager.getUrl().lastIndexOf("/") + 1);
        when(fileManagerRepository.findByIdAndUserId(FILE_ID, USER_ID)).thenReturn(Optional.of(fileManager));
        when(s3Client.deleteObject(any(DeleteObjectRequest.class))).thenReturn(DeleteObjectResponse.builder().build());
        fileManagerService.deleteFile(FILE_ID, USER_ID);
        verify(fileManagerRepository).deleteById(FILE_ID);
        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void Should_ThrowInvalidFileException_When_InvalidFileIdAndUserIdIsProvidedForDeleteFile() {
        when(fileManagerRepository.findByIdAndUserId(FILE_ID, USER_ID)).thenReturn(Optional.empty());
        InvalidFileException invalidFileException = assertThrows(InvalidFileException.class, () ->
                fileManagerService.deleteFile(FILE_ID, USER_ID));
        assertEquals("File not found", invalidFileException.getMessage());
    }

    @Test
    void Should_ThrowFileManagerException_When_GetFileManagerFromDataBaseIsFailedForDeleteFile() {
        when(fileManagerRepository.findByIdAndUserId(FILE_ID, USER_ID)).thenThrow(new DataAccessException("ERROR") {
        });
        FileManagerException fileManagerException = assertThrows(FileManagerException.class, () ->
                fileManagerService.deleteFile(FILE_ID, USER_ID));
        assertEquals("Error while getting file url ", fileManagerException.getMessage());
    }

    @Test
    void Should_ThrowFileManagerException_When_DeleteFileReferenceFromDataBaseIsFailedForDeleteFile() {
        FileManager fileManager = getSampleFileManager();
        when(fileManagerRepository.findByIdAndUserId(FILE_ID, USER_ID)).thenReturn(Optional.of(fileManager));
        doThrow(new DataAccessException("ERROR") {
        }).when(fileManagerRepository).deleteById(FILE_ID);
        FileManagerException fileManagerException = assertThrows(FileManagerException.class, () ->
                fileManagerService.deleteFile(FILE_ID, USER_ID));
        assertEquals("Error while deleting the file reference ", fileManagerException.getMessage());
    }

    @Test
    void Should_ThrowFileManagerException_When_DeleteFileFromAmazonIsFailedForDeleteFile() {
        FileManager fileManager = getSampleFileManager();
        String fileNameWithExtension = fileManager.getUrl().substring(fileManager.getUrl().lastIndexOf("/") + 1);
        when(fileManagerRepository.findByIdAndUserId(FILE_ID, USER_ID)).thenReturn(Optional.of(fileManager));
        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .thenThrow(SdkException.builder().message("ERROR").build());
        FileManagerException fileManagerException = assertThrows(FileManagerException.class, () ->
                fileManagerService.deleteFile(FILE_ID, USER_ID));
        assertEquals("Error while deleting the file ", fileManagerException.getMessage());
    }

    @Test
    void Should_ReturnFileManager_When_ValidFileIdAndUserIdIsProvidedForGetFileManager() {
        FileManager fileManager = getSampleFileManager();
        when(fileManagerRepository.findByIdAndUserId(FILE_ID, USER_ID)).thenReturn(Optional.of(fileManager));
        assertEquals(fileManager, fileManagerService.getFileDetail(FILE_ID, USER_ID));
    }

    @Test
    void Should_ThrowInvalidFileException_When_InvalidFileIdAndUserIdIsProvidedForGetFileManager() {
        when(fileManagerRepository.findByIdAndUserId(FILE_ID, USER_ID)).thenReturn(Optional.empty());
        InvalidFileException invalidFileException = assertThrows(InvalidFileException.class, () ->
                fileManagerService.getFileDetail(FILE_ID, USER_ID));
        assertEquals("File not found", invalidFileException.getMessage());
    }

    @Test
    void Should_ThrowFileManagerException_When_GetFileManagerFromDataBaseIsFailedForGetFileManager() {
        when(fileManagerRepository.findByIdAndUserId(FILE_ID, USER_ID)).thenThrow(new DataAccessException("ERROR") {
        });
        FileManagerException fileManagerException = assertThrows(FileManagerException.class, () ->
                fileManagerService.getFileDetail(FILE_ID, USER_ID));
        assertEquals("Error while getting file detail", fileManagerException.getMessage());
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
        return PageRequest.of(0, 1);

    }

    /**
     * This method creates sample FileUpdateRequestDto
     *
     * @return FileUpdateRequestDto
     */
    private FileUpdateRequestDto getSampleFileUpdateRequestDto() {
        FileUpdateRequestDto fileUpdateRequestDto = new FileUpdateRequestDto();
        fileUpdateRequestDto.setFileId(FILE_ID);
        fileUpdateRequestDto.setDescription("Description");
        fileUpdateRequestDto.setName("Name");
        return fileUpdateRequestDto;
    }
}
