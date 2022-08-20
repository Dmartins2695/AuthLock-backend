package com.webapp.pwmanager.registration.service;

import com.webapp.pwmanager.registration.model.ConfirmationEmailDto;
import com.webapp.pwmanager.registration.model.RegistrationDto;
import org.springframework.http.ResponseEntity;

import javax.validation.Valid;


public interface RegistrationService {


    ResponseEntity<?> register(@Valid RegistrationDto request);

    ResponseEntity<?> resendConfirmationEmail(@Valid ConfirmationEmailDto request);

    ResponseEntity<?> confirmToken(String token);
}
