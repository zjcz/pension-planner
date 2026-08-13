package com.pensionplanner.auth;

import com.pensionplanner.user.User;

import java.time.Instant;

public record UserDto(Long id, String username, Instant createdAt) {

    public static UserDto from(User user) {
        return new UserDto(user.getUserId(), user.getUsername(), user.getCreatedAt());
    }
}
