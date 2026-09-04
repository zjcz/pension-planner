package com.pensionplanner.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pension-planner.rate-limit")
public record RateLimitProperties(boolean enabled, int capacity, int refillPerMinute) {

    public RateLimitProperties {
        if (capacity < 1) {
            capacity = 20;
        }
        if (refillPerMinute < 1) {
            refillPerMinute = 10;
        }
    }
}