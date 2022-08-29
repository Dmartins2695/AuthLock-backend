package com.webapp.pwmanager.controller;

import com.webapp.pwmanager.service.AppUserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/user")
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600, methods = {RequestMethod.GET, RequestMethod.POST},allowCredentials = "true")
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
