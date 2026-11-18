package com.kupi.cc.base;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableDiscoveryClient
@ServletComponentScan
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class KupiCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(KupiCoreApplication.class, args);
    }

}
