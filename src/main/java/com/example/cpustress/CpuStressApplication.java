package com.example.cpustress;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(CpuConsumerProperties.class)
public class CpuStressApplication {
    public static void main(String[] args) {
        SpringApplication.run(CpuStressApplication.class, args);
    }
}
