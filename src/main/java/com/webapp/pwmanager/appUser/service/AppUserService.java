package com.webapp.pwmanager.appUser.service;

import com.webapp.pwmanager.appUser.domain.AppUser;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


public interface AppUserService extends UserDetailsService {

    AppUser loadUserByUsername(String email) throws UsernameNotFoundException;

    boolean singUpUser(AppUser appUser, String token);

    void enableAppUser(String email);

    boolean hasUser(String email);

    void removeAppUserNotConfirmed(String email);
}
