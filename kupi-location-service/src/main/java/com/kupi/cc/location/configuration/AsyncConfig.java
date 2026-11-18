package com.kupi.cc.location.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Core pool size - minimum threads always running
        executor.setCorePoolSize(5);

        // Maximum pool size - max threads when queue is full
        executor.setMaxPoolSize(20);

        // Queue capacity - how many tasks can wait
        executor.setQueueCapacity(100);

        // Thread name prefix for easier debugging
        executor.setThreadNamePrefix("notification-");

        // Allow core threads to timeout and shutdown
        executor.setAllowCoreThreadTimeOut(true);

        // Keep alive time for idle threads
        executor.setKeepAliveSeconds(60);

        // Initialize the executor
        executor.initialize();

        return executor;
    }
}