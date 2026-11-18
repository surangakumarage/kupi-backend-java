package com.kupi.fileuploader.service;

import com.kupi.fileuploader.domain.response.CsvS3UploadResponseDto;
import com.kupi.fileuploader.exception.FileManagerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Csv Service
 */
@Service
public class CsvService {

    private static final String TIMESTAMP_PREFIX = "_TIME_STAMP_";
    private static final String DATE_TIME_FORMATTER_PATTERN = "yyyy_MM_dd_HH_mm_ss";
    private static final String CSV_UPLOAD_ERROR = "Uploading csv to S3 bucket was filed";
    private static final String DOT = ".";
    private final S3Client s3Client;
    private final String bucketName;
    private final String storageProvider;
    private final String awsRegion;

    @Autowired
    public CsvService(@Qualifier("doS3Client") S3Client s3Client, @Value("${aws.bucket.name}") String bucketName,
                      @Value("${aws.bucket.paths.coupon}") String couponPath, @Value("${file.storage.provider}")
                      String storageProvider, @Value("${aws.region}") String awsRegion) {
        this.s3Client = s3Client;
        this.storageProvider = storageProvider;
        this.bucketName = bucketName + couponPath;
        this.awsRegion = awsRegion;
    }

    /**
     * This method stores the given csv in s3 bucket and returns public access url.
     *
     * @param csv csv
     * @return public access url
     */
    public CsvS3UploadResponseDto uploadCsv(MultipartFile csv, String fileName) {
        try {
            String formattedDate = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern(DATE_TIME_FORMATTER_PATTERN));
            String fileExtension = StringUtils.getFilenameExtension(csv.getOriginalFilename());
            String csvName = fileName + TIMESTAMP_PREFIX + formattedDate + DOT + fileExtension;

            PutObjectRequest.Builder putObjectBuilder = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(csvName)
                    .contentType(csv.getContentType())
                    .contentLength(csv.getSize());

            if (isDigitalOcean()) {
                putObjectBuilder.acl(ObjectCannedACL.PUBLIC_READ);
            }

            PutObjectRequest putObjectRequest = putObjectBuilder.build();
            s3Client.putObject(putObjectRequest,
                    software.amazon.awssdk.core.sync.RequestBody.fromInputStream(csv.getInputStream(), csv.getSize()));

            String csvUrlString = constructS3Url(csvName);
            URL csvUrl = new URL(csvUrlString);
            return new CsvS3UploadResponseDto(csvUrl, csvName);
        } catch (SdkException | IOException e) {
            throw new FileManagerException(CSV_UPLOAD_ERROR, e);
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
            // DigitalOcean Spaces URL format
            return String.format("https://%s.cdn.digitaloceanspaces.com/%s", bucketName.split("/")[0], key);
        } else {
            // AWS S3 URL format
            return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, awsRegion, key);
        }
    }

    private boolean isDigitalOcean() {
        return "do".equalsIgnoreCase(storageProvider);
    }

}
