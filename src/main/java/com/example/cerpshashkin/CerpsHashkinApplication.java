package com.example.cerpshashkin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CerpsHashkinApplication {

    private CerpsHashkinApplication() {
    }

    public static void main(final String[] args) {
        SpringApplication.run(CerpsHashkinApplication.class, args);
    }
}

