package com.webapp.pwmanager.appUser.web;

import com.webapp.pwmanager.appUser.repository.AppUserRepository;
import com.webapp.pwmanager.appUser.domain.AppUser;
import com.webapp.pwmanager.appUser.service.AppUserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/user")
@AllArgsConstructor
public class AppUserController {

    private final AppUserService appUserService;

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentUser() {
        return appUserService.getCurrentUser();
    }

    @GetMapping("/stored-passwords/{userId}")
    public ResponseEntity<?> getUserStoredPasswords(@Valid @PathVariable Long userId) {
        return appUserService.getUserStoredPasswords(userId);
    }
}
