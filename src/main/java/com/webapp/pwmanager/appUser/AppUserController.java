package com.webapp.pwmanager.appUser;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/v1/app-users")
@AllArgsConstructor
public class AppUserController {

    private final AppUserRepository appUserRepository;

    @GetMapping(path = "{appUserEmail}")
    public Optional<AppUser> getAppUser(@PathVariable("appUserEmail") String appUserEmail) {
        return appUserRepository.findByEmail(appUserEmail);
    }
}
