package com.kupi.fileuploader.configurations;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This holds aws configurations.
 */
@Configuration
public class AwsS3Config {

    @Bean
    public S3Client awsS3Client() {
        return S3Client.builder()
                .region(Region.AP_SOUTHEAST_1)
                .build();
    }
}
