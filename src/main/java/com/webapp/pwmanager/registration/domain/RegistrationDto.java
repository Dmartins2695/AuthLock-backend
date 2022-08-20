package com.webapp.pwmanager.registration.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class RegistrationDto {
    private final String email;
    private final String password;
    private final String firstName;
    private final String lastName;
}
