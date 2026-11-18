package com.kupi.cc.base.configuration;


// import com.google.analytics.data.v1beta.BetaAnalyticsDataClient;
import com.kupi.cc.base.exception.KupiCoreException;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * MIGRATION NOTE: Google Analytics integration disabled for Java 21 + Spring Boot 3.2.6 migration
 * The google-analytics-data:0.42.1 dependency is not available in Maven Central.
 *
 * To re-enable this feature:
 * 1. Update pom.xml with a compatible version (e.g., 0.37.0 or later)
 * 2. Uncomment the import and bean below
 * 3. Update ReportService to re-enable analytics queries
 *
 * For now, ReportService methods that use BetaAnalyticsDataClient will need to be stubbed or disabled.
 */
@Component
public class GoogleAnalyticsReportConfiguration {

    /**
     * Initializes an Analytics data api v1.
     *
     * @return analytics data client.
     */
    // @Bean
    // public BetaAnalyticsDataClient createAnalyticsConnection() {
    //     try {
    //         return BetaAnalyticsDataClient.create();
    //     } catch (IOException e) {
    //         throw new KupiCoreException("Initializing google analytics failed", e);
    //     }
    // }
}
