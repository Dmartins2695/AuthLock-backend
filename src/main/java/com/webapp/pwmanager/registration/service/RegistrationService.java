package com.webapp.pwmanager.registration.service;

import com.webapp.pwmanager.appUser.domain.AppUser;
import com.webapp.pwmanager.registration.domain.ConfirmationEmailDto;
import com.webapp.pwmanager.registration.domain.RegistrationDto;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;


public interface RegistrationService {


    ResponseEntity<?> register(@Validated RegistrationDto request);

    ResponseEntity<?> resendConfirmationEmail(@Validated ConfirmationEmailDto request);

    ResponseEntity<?> confirmToken(String token);
}
