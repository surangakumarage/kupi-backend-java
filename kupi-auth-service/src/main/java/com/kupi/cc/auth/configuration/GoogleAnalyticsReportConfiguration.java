package com.kupi.cc.auth.configuration;

import org.springframework.context.annotation.Configuration;

/**
 * Class to handle Google Analytics API's
 *
 * NOTE: Google Analytics functionality has been disabled as the google-analytics-data library
 * is not available in Spring Boot 3.2.6 migration. This can be re-enabled when the library
 * is properly migrated or a replacement analytics service is implemented.
 */

@Configuration
public class GoogleAnalyticsReportConfiguration {

    /**
     * Initializes an Analytics data api v1.
     * DISABLED: BetaAnalyticsDataClient not available in this Spring Boot version
     *
     * @return analytics data client.
     */
    /*
    @Bean
    public BetaAnalyticsDataClient createAnalyticsConnection() {
        try {
            return BetaAnalyticsDataClient.create();
        } catch (IOException e) {
            throw new AuthServiceException("Initializing google analytics failed", e);
        }
    }
    */
}

