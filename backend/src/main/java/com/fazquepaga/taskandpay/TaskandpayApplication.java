package com.fazquepaga.taskandpay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TaskandpayApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskandpayApplication.class, args);
    }
}
