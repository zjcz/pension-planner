package com.pensionplanner.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pension-planner.jwt")
public record JwtProperties(String secret, long ttlSeconds, String cookieName, boolean secure) {

    public JwtProperties {
        ttlSeconds = ttlSeconds == 0 ? 604800 : ttlSeconds;
        cookieName = cookieName == null || cookieName.isBlank() ? "pp_jwt" : cookieName;
    }
}
