package com.petcare;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.petcare.mapper")
@EnableScheduling
public class PetCareApplication {
    public static void main(String[] args) {
        SpringApplication.run(PetCareApplication.class, args);
    }
}
