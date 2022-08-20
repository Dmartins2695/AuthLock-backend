package com.webapp.pwmanager.registration.web;

import com.webapp.pwmanager.registration.model.ConfirmationEmailDto;
import com.webapp.pwmanager.registration.model.RegistrationDto;
import com.webapp.pwmanager.registration.service.RegistrationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(path = "api/v1/registration")
@AllArgsConstructor
public class RegistrationController {
    private RegistrationService registrationService;

    @PostMapping
    public ResponseEntity<?> register(@Valid @RequestBody RegistrationDto request) {
        return registrationService.register(request);
    }

    @PostMapping(path = "resend-confirmation-email")
    public ResponseEntity<?> resendConfirmationEmail(@Valid @RequestBody ConfirmationEmailDto request) {
        return registrationService.resendConfirmationEmail(request);
    }

    @GetMapping(path = "confirm")
    public ResponseEntity<?> confirm(@Valid @RequestParam("token") String token) {
        return registrationService.confirmToken(token);
    }
}
