package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class Crud139Application {
    public static void main(String[] args) {
        SpringApplication.run(Crud139Application.class, args);
        //System.out.println(new BCryptPasswordEncoder().encode("123456"));
    }
}