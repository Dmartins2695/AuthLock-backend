package com.webapp.pwmanager.appUser;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("api/v1/app-users")
public class AppUserController {

    private static final List<AppUser> APP_USER_LIST = Arrays.asList(
            new AppUser(1, "appUser 1"),
            new AppUser(2, "appUser 2")
    );

    @GetMapping(path = "{appUserId}")
    public AppUser getAppUser(@PathVariable("appUserId") Integer appUserId) {
        return APP_USER_LIST
                .stream()
                .filter(appUser -> appUserId.equals(appUser.getAppUserId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("AppUser " + appUserId + "id not found"));
    }
}
