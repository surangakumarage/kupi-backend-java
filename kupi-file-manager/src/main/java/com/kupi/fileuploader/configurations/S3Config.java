package com.kupi.fileuploader.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import java.net.URI;


@Configuration
public class S3Config {

    @Value("${file.storage.provider}")
    private String storageProvider;

    @Value("${aws.access-key}")
    private String awsAccessKey;

    @Value("${aws.secret-key}")
    private String awsSecretKey;

    @Value("${aws.region}")
    private String awsRegion;

    @Value("${do.access-key}")
    private String doAccessKey;

    @Value("${do.secret-key}")
    private String doSecretKey;

    @Value("${do.region}")
    private String doRegion;

    @Value("${do.endpoint}")
    private String doEndpoint;

    @Bean("doS3Client")
    @Primary
    public S3Client amazonS3() {
        S3ClientBuilder builder = S3Client.builder();

        if ("do".equalsIgnoreCase(storageProvider)) {
            // DigitalOcean Spaces configuration
            AwsBasicCredentials credentials = AwsBasicCredentials.create(doAccessKey, doSecretKey);

            builder.credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .endpointOverride(URI.create(doEndpoint))
                    .region(Region.of(doRegion));
        } else {
            // AWS S3 configuration
            AwsBasicCredentials credentials = AwsBasicCredentials.create(awsAccessKey, awsSecretKey);

            builder.credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .region(Region.of(awsRegion));
        }

        return builder.build();
    }
}
