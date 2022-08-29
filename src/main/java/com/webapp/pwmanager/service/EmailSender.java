package com.webapp.pwmanager.service;

import com.webapp.pwmanager.domain.Email;

public interface EmailSender {
    void send(Email email);
}