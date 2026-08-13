package com.pensionplanner.security;

import com.pensionplanner.config.JwtProperties;
import com.pensionplanner.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService(
            new JwtProperties("test-secret-for-pension-planner", 3600, "pp_jwt", false));

    @Test
    void createAndParseToken() {
        String token = jwtService.createToken(user(42L, "alice"));

        CurrentUser parsed = jwtService.parseToken(token);

        assertThat(parsed.userId()).isEqualTo(42L);
        assertThat(parsed.username()).isEqualTo("alice");
    }

    @Test
    void rejectsTamperedToken() {
        String token = jwtService.createToken(user(1L, "bob"));
        String tampered = token.substring(0, token.length() - 4) + "xxxx";

        assertThatThrownBy(() -> jwtService.parseToken(tampered)).isInstanceOf(Exception.class);
    }

    @Test
    void cookieIsHttpOnlySameSiteLax() {
        ResponseCookie cookie = jwtService.authCookie("token-value");

        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getSameSite()).isEqualTo("Lax");
        assertThat(cookie.getMaxAge().getSeconds()).isEqualTo(3600);
        assertThat(cookie.isSecure()).isFalse();
    }

    @Test
    void clearCookieExpiresImmediately() {
        ResponseCookie cookie = jwtService.clearCookie();

        assertThat(cookie.getValue()).isEmpty();
        assertThat(cookie.getMaxAge().getSeconds()).isZero();
    }

    @Test
    void randomSecretIsGeneratedWhenBlank() {
        JwtService first = new JwtService(new JwtProperties("", 3600, "pp_jwt", false));
        JwtService second = new JwtService(new JwtProperties("   ", 3600, "pp_jwt", false));

        assertThat(first.createToken(user(1L, "x")))
                .isNotEqualTo(second.createToken(user(1L, "x")));
    }

    private User user(Long id, String username) {
        User user = new User();
        user.setUserId(id);
        user.setUsername(username);
        return user;
    }
}
