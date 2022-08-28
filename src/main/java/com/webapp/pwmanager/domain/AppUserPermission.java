package com.webapp.pwmanager.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AppUserPermission {
    PASSWORD_READ("password:read"),
    PASSWORD_WRITE("password:write");

    private final String permission;

}