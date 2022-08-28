package com.webapp.pwmanager.service;

import com.webapp.pwmanager.dto.ConfirmationEmailDto;
import com.webapp.pwmanager.dto.RegistrationDto;
import org.springframework.http.ResponseEntity;

import javax.validation.Valid;


public interface RegistrationService {


    ResponseEntity<?> register(@Valid RegistrationDto request);

    ResponseEntity<?> resendConfirmationEmail(@Valid ConfirmationEmailDto request);

    ResponseEntity<?> confirmToken(String token);
}
