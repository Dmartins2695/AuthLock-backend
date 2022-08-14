package com.webapp.pwmanager.appUser;

public class AppUser {
    private final Integer appUserId;
    private final String appUserName;

    public AppUser(Integer appUserId, String appUserName) {
        this.appUserId = appUserId;
        this.appUserName = appUserName;
    }

    public Integer getAppUserId() {
        return appUserId;
    }

    public String getAppUserName() {
        return appUserName;
    }
}
