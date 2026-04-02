package com.bank.swift;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SwiftMiddlewareApplication {

    public static void main(String[] args) {
        SpringApplication.run(SwiftMiddlewareApplication.class, args);
    }
}
