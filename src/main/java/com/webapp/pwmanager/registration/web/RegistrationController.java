package com.webapp.pwmanager.registration.web;

import com.webapp.pwmanager.registration.service.RegistrationService;
import com.webapp.pwmanager.registration.domain.ConfirmationEmailDto;
import com.webapp.pwmanager.registration.domain.RegistrationDto;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "api/v1/registration")
@AllArgsConstructor
public class RegistrationController {
    private RegistrationService registrationService;

    @PostMapping
    public ResponseEntity<?> register(@Validated @RequestBody RegistrationDto request) {
        return registrationService.register(request);
    }

    @PostMapping(path = "resend-confirmation-email")
    public ResponseEntity<?> resendConfirmationEmail(@Validated @RequestBody ConfirmationEmailDto request) {
        return registrationService.resendConfirmationEmail(request);
    }

    @GetMapping(path = "confirm")
    public ResponseEntity<?> confirm(@Validated @RequestParam("token") String token) {
        return registrationService.confirmToken(token);
    }
}
