package dev.jonclarke.pensionplanner.auth;

import dev.jonclarke.pensionplanner.user.User;

import java.time.Instant;

public record UserDto(Long id, String username, Instant createdAt) {

    public static UserDto from(User user) {
        return new UserDto(user.getUserId(), user.getUsername(), user.getCreatedAt());
    }
}
