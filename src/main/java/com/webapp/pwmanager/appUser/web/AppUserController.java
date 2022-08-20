package com.webapp.pwmanager.appUser.web;

import com.webapp.pwmanager.appUser.repository.AppUserRepository;
import com.webapp.pwmanager.appUser.domain.AppUser;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/user")
@AllArgsConstructor
public class AppUserController {

    private final AppUserRepository appUserRepository;

    @GetMapping()
    public Optional<AppUser> getAppUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String currentUserName = authentication.getName();
            return appUserRepository.findByEmail(currentUserName);
        }
        return Optional.empty();
    }
}