package com.webapp.pwmanager.appUser.web;

import com.webapp.pwmanager.appUser.service.AppUserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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
