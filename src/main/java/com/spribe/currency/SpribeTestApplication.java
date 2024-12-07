package com.spribe.currency;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableFeignClients
@EnableScheduling
@SpringBootApplication
public class SpribeTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpribeTestApplication.class, args);
    }

}