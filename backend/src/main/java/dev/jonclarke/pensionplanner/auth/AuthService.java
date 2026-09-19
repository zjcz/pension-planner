package dev.jonclarke.pensionplanner.auth;

import dev.jonclarke.pensionplanner.common.ApiException;
import dev.jonclarke.pensionplanner.user.User;
import dev.jonclarke.pensionplanner.user.UserRepository;
import dev.jonclarke.pensionplanner.user.UserSettings;
import dev.jonclarke.pensionplanner.user.UserSettingsRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean allowRegistration;

    public AuthService(UserRepository userRepository, UserSettingsRepository userSettingsRepository,
                       PasswordEncoder passwordEncoder,
                       @Value("${ALLOW_REGISTRATION:true}") boolean allowRegistration) {
        this.userRepository = userRepository;
        this.userSettingsRepository = userSettingsRepository;
        this.passwordEncoder = passwordEncoder;
        this.allowRegistration = allowRegistration;
    }

    @Transactional
    public User register(String username, String password) {
        if (!allowRegistration) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Registration is disabled");
        }
        String normalizedUsername = username.trim();
        if (userRepository.existsByUsername(normalizedUsername)) {
            throw new ApiException(HttpStatus.CONFLICT, "Username is already taken");
        }

        User user = new User();
        user.setUsername(normalizedUsername);
        user.setPasswordHash(passwordEncoder.encode(password));
        User saved = userRepository.save(user);

        UserSettings settings = new UserSettings();
        settings.setUserId(saved.getUserId());
        userSettingsRepository.save(settings);

        return saved;
    }

    public User login(String username, String password) {
        User user = userRepository.findByUsername(username.trim())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }
        return user;
    }

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User no longer exists"));
    }
}
