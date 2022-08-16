package com.webapp.pwmanager.email;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
@Getter
@Setter
@AllArgsConstructor
public class Email {
    private Map<String, Object> properties;
    private String to;
    private String from;
    private String subject;
    private String template;
}
