package com.wisewealth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class WisewealthApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(WisewealthApiApplication.class, args);
    }

}
