package com.pensionplanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PensionPlannerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PensionPlannerApplication.class, args);
    }
}
