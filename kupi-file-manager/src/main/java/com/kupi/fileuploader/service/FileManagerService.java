package com.kupi.fileuploader.service;

import com.kupi.fileuploader.domain.FileManagerDto;
import com.kupi.fileuploader.domain.entity.FileManager;
import com.kupi.fileuploader.domain.request.FileUpdateRequestDto;
import com.kupi.fileuploader.domain.response.*;
import com.kupi.fileuploader.enums.FileTypes;
import com.kupi.fileuploader.exception.FileManagerException;
import com.kupi.fileuploader.exception.InvalidFileException;
import com.kupi.fileuploader.repository.FileManagerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * File Upload service
 */
@Service
@Slf4j
public class FileManagerService {

    private static final String FILE_UPLOAD_ERROR = "Uploading file to S3 bucket was filed";
    private static final String FILE_DOWNLOAD_ERROR = "Error occurred while downloading the file";
    private static final String FILE_SUMMARY_ERROR = "Error occurred while getting file summary";
    private static final String NO_FILE_FOUND = "File does not exists in s3";
    private static final String FILE_ID_PREFIX = "fid-";
    private static final String DOT = ".";
    private static final String FILE_NOT_FOUND = "File not found";
    private static final String DEFAULT_FILTER_OPTION = "NONE";
    private final S3Client s3Client;
    private final String bucketName;
    private final FileManagerRepository fileManagerRepository;
    private final long maxByteSize;

    private final String storageProvider;
    private final String awsRegion;

    @Autowired
    public FileManagerService(@Qualifier("doS3Client") S3Client s3Client, @Value("${aws.bucket.name}") String bucketName,
                              FileManagerRepository fileManagerRepository,
                              @Value("${file.maxByteSize}") long maxByteSize, @Value("${file.storage.provider}")
                              String storageProvider, @Value("${aws.region}") String awsRegion) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.fileManagerRepository = fileManagerRepository;
        this.maxByteSize = maxByteSize;
        this.storageProvider = storageProvider;
        this.awsRegion = awsRegion;
    }

    /**
     * Upload file to s3
     *
     * @param uploadFile uploadFile
     * @param fileName   fileName
     * @param userId     userId
     * @return fileResponseDto
     */
    public FileResponseDto uploadFile(MultipartFile uploadFile, String fileName, String userId) {
        try {
            String fileId = FILE_ID_PREFIX + UUID.randomUUID();
            String fileExtension = StringUtils.getFilenameExtension(uploadFile.getOriginalFilename());
            String fileUrlWithExtension = fileId + DOT + fileExtension;

            PutObjectRequest.Builder putObjectBuilder = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileUrlWithExtension)
                    .contentType(uploadFile.getContentType())
                    .contentLength(uploadFile.getSize());

            if (isUsingDigitalOceanStorageProvider()) {
                putObjectBuilder.acl(ObjectCannedACL.PUBLIC_READ);
            }

            PutObjectRequest putObjectRequest = putObjectBuilder.build();
            s3Client.putObject(putObjectRequest,
                    software.amazon.awssdk.core.sync.RequestBody.fromInputStream(uploadFile.getInputStream(), uploadFile.getSize()));

            String url = constructS3Url(fileUrlWithExtension);
            FileManagerDto fileManagerDto = new FileManagerDto(uploadFile.getContentType(), uploadFile.getSize(), url, fileName, userId);
            saveFileMetaData(fileManagerDto, fileId);
            return new FileResponseDto(uploadFile, url, fileName);
        } catch (SdkException | IOException e) {
            throw new FileManagerException(FILE_UPLOAD_ERROR, e);
        }
    }

    /**
     * Save file metadata
     *
     * @param fileManagerDto fileManagerDto
     * @param fileId         fileId
     */
    private void saveFileMetaData(FileManagerDto fileManagerDto, String fileId) {
        try {
            FileManager fileManager = new FileManager(fileManagerDto, fileId);
            fileManagerRepository.save(fileManager);
        } catch (DataAccessException e) {
            throw new FileManagerException("Error while saving file metadata", e);
        }
    }

    /**
     * Download a file from s3 bucket when fileName is provided
     *
     * @param fileId keyName
     * @return byte
     * @throws NoSuchFileException NoSuchFileException
     */
    public byte[] downloadFile(final String fileId) throws NoSuchFileException {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileId)
                    .build();

            InputStream response = s3Client.getObject(getObjectRequest);
            byte[] content = response.readAllBytes();
            response.close();
            return content;
        } catch (NoSuchKeyException e) {
            throw new NoSuchFileException(NO_FILE_FOUND);
        } catch (IOException | SdkException ex) {
            throw new FileManagerException(FILE_DOWNLOAD_ERROR, ex);
        }
    }

    /**
     * This method returns the file with extension stored in database
     *
     * @param fileId fileId
     * @param userId userId
     * @return file name
     */
    public String getFileNameWithExtension(String fileId, String userId) {
        try {
            Optional<FileManager> fileManager = fileManagerRepository.findByIdAndUserId(fileId, userId);
            if (fileManager.isPresent()) {
                String fileUrl = fileManager.get().getUrl();
                return fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            } else {
                throw new InvalidFileException(FILE_NOT_FOUND);
            }
        } catch (DataAccessException e) {
            throw new FileManagerException("Error while getting file url ", e);
        }
    }

    /**
     * This method returns the all file summary
     *
     * @param pageable pageable
     * @return FileSummaryPageResponse
     */
    public FileSummaryPageResponse getAllFileSummary(String userId, Pageable pageable) {
        try {
            Page<FileManager> fileManagerPage = getAllFiles(userId, pageable);
            return buildFileSummaryResponseObjects(userId, fileManagerPage);
        } catch (DataAccessException e) {
            throw new FileManagerException(FILE_SUMMARY_ERROR, e);
        }
    }

    /**
     * This method returns file summary by file type
     *
     * @param userId   userId
     * @param option   option
     * @param name     name
     * @param pageable pageable
     * @return FileSummaryPageResponse
     */
    public FileSummaryPageResponse getFileSummaryByNameOrType(String userId, String option,
                                                              String name, Pageable pageable) {
        Page<FileManager> fileManagerPage = getFilesByType(userId, option, name, pageable);
        return buildFileSummaryResponseObjects(userId, fileManagerPage);
    }

    /**
     * Build the summary response objects
     *
     * @param userId          userId
     * @param fileManagerPage fileManagerPage
     * @return FileSummaryPageResponse
     */
    private FileSummaryPageResponse buildFileSummaryResponseObjects(String userId, Page<FileManager> fileManagerPage) {
        FileVolumeResponse fileVolumeResponse = new FileVolumeResponse(maxByteSize, getRemainingFileSize(userId));
        FileSummaryResponse fileSummaryResponse = new FileSummaryResponse(fileVolumeResponse, FileTypes.getFileTypes());
        FileListPageResponse fileListPageResponse = new FileListPageResponse(fileManagerPage.getContent());
        return new FileSummaryPageResponse(fileManagerPage, fileSummaryResponse, fileListPageResponse.getFiles());
    }

    /**
     * Get remaining total file size
     *
     * @param userId userId
     * @return total fileSize
     */
    private long getRemainingFileSize(String userId) {
        try {
            long totalSize = fileManagerRepository.calculateTotalSize(userId);
            return maxByteSize - totalSize;
        } catch (DataAccessException e) {
            throw new FileManagerException("Error while getting total file size");
        }
    }

    /**
     * Get all file details
     *
     * @param userId   userId
     * @param pageable pageable
     * @return file page
     */
    private Page<FileManager> getAllFiles(String userId, Pageable pageable) {
        try {
            return fileManagerRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        } catch (DataAccessException e) {
            throw new FileManagerException("Error while getting file details", e);
        }
    }

    /**
     * Get files by file type
     *
     * @param userId   userId
     * @param option   option
     * @param pageable pageable
     * @return page of file
     */
    private Page<FileManager> getFilesByType(String userId, String option, String name, Pageable pageable) {
        try {
            if (name.equals(DEFAULT_FILTER_OPTION)) {
                return fileManagerRepository.findByUserIdAndFileType(userId, option, "", pageable);
            } else if (option.equals(DEFAULT_FILTER_OPTION)) {
                return fileManagerRepository.findByUserIdAndFileType(userId, "", name, pageable);
            }
            return fileManagerRepository.findByUserIdAndFileType(userId, option, name, pageable);
        } catch (DataAccessException e) {
            throw new FileManagerException("Error while getting file details", e);
        }
    }

    /**
     * This method delete the file from s3 and file reference
     *
     * @param fileId fileId
     */
    public void deleteFile(String fileId, String userId) {
        try {
            String fileNameWithExtension = getFileNameWithExtension(fileId, userId);
            deleteFileReference(fileId);
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileNameWithExtension)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
        } catch (SdkException e) {
            throw new FileManagerException("Error while deleting the file ", e);
        }
    }

    /**
     * Delete the file reference
     *
     * @param fileId fileId
     */
    private void deleteFileReference(String fileId) {
        try {
            fileManagerRepository.deleteById(fileId);
        } catch (DataAccessException e) {
            throw new FileManagerException("Error while deleting the file reference ", e);
        }
    }

    /**
     * This method checks the existence of file reference
     *
     * @param fileId fileId
     * @param userId userId
     * @return true/false
     */
    private boolean isFileExists(String fileId, String userId) {
        try {
            return fileManagerRepository.existsByIdAndUserId(fileId, userId);
        } catch (DataAccessException e) {
            throw new FileManagerException("Error while checking the file reference ", e);
        }
    }

    /**
     * This method updates file details
     *
     * @param fileUpdateRequestDto fileUpdateRequestDto
     * @param userId               userId
     */
    public void updateFile(FileUpdateRequestDto fileUpdateRequestDto, String userId) {
        try {
            Optional<FileManager> fileManager = fileManagerRepository
                    .findByIdAndUserId(fileUpdateRequestDto.getFileId(), userId);
            if (fileManager.isPresent()) {
                FileManager file = fileManager.get();
                file.setName(fileUpdateRequestDto.getName());
                file.setDescription(fileUpdateRequestDto.getDescription() == null ? null :
                        fileUpdateRequestDto.getDescription());
                file.setUpdatedAt(new Date());
                fileManagerRepository.save(file);
            } else {
                throw new InvalidFileException(FILE_NOT_FOUND);
            }
        } catch (DataAccessException e) {
            throw new FileManagerException("Error while updating the file details ", e);
        }
    }

    /**
     * This method returns file detail
     *
     * @param fileId fileId
     * @param userId userId
     * @return fileManager
     */
    public FileManager getFileDetail(String fileId, String userId) {
        try {
            Optional<FileManager> fileManager = fileManagerRepository
                    .findByIdAndUserId(fileId, userId);
            if (fileManager.isPresent()) {
                return fileManager.get();
            } else {
                throw new InvalidFileException(FILE_NOT_FOUND);
            }
        } catch (DataAccessException e) {
            throw new FileManagerException("Error while getting file detail", e);
        }
    }

    /**
     * Constructs S3 URL for the given key
     *
     * @param key object key
     * @return S3 URL
     */
    private String constructS3Url(String key) {
        if ("do".equalsIgnoreCase(storageProvider)) {
            // DigitalOcean Spaces URL format - will use endpoint from config
            return String.format("https://%s.cdn.digitaloceanspaces.com/%s", bucketName.split("/")[0], key);
        } else {
            // AWS S3 URL format
            return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, awsRegion, key);
        }
    }

    private boolean isUsingDigitalOceanStorageProvider() {
        return "do".equalsIgnoreCase(storageProvider);
    }

}
