package com.pensionplanner.auth;

import com.pensionplanner.security.CurrentUserService;
import com.pensionplanner.security.JwtService;
import com.pensionplanner.user.User;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final CurrentUserService currentUserService;
    private final boolean allowRegistration;

    public AuthController(AuthService authService, JwtService jwtService,
                          CurrentUserService currentUserService,
                          @Value("${ALLOW_REGISTRATION:true}") boolean allowRegistration) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.currentUserService = currentUserService;
        this.allowRegistration = allowRegistration;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request.username(), request.password());
        ResponseCookie cookie = jwtService.authCookie(jwtService.createToken(user));
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(UserDto.from(user));
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@Valid @RequestBody LoginRequest request) {
        User user = authService.login(request.username(), request.password());
        ResponseCookie cookie = jwtService.authCookie(jwtService.createToken(user));
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(UserDto.from(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtService.clearCookie().toString())
                .build();
    }

    @GetMapping("/me")
    public UserDto me() {
        return UserDto.from(authService.findById(currentUserService.currentUserId()));
    }

    @GetMapping("/config")
    public AuthConfigResponse config() {
        return new AuthConfigResponse(allowRegistration);
    }
}
