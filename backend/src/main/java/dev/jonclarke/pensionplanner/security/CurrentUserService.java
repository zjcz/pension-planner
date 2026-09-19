package dev.jonclarke.pensionplanner.security;

import dev.jonclarke.pensionplanner.common.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    public CurrentUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CurrentUser user) {
            return user;
        }
        throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication required");
    }

    public Long currentUserId() {
        return currentUser().userId();
    }
}
