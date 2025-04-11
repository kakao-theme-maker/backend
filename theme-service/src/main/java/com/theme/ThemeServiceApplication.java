package com.theme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EntityScan(basePackages = "com.theme.domain")
@EnableDiscoveryClient
public class ThemeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ThemeServiceApplication.class, args);
    }
}
