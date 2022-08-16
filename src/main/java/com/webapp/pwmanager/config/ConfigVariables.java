package com.webapp.pwmanager.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Getter
@NoArgsConstructor
@Component
public class ConfigVariables {
    //dev
    private final String emailFrom = "daniel@gmail.com";
    private final String confirmLink = "http://localhost:8080/api/v1/registration/confirm?token=";
    //prod
    /*
    public String emailFrom = "daniel@gmail.com";
    public String confirmLink = "http://localhost:8080/api/v1/registration/confirm?token=";
    */
}
