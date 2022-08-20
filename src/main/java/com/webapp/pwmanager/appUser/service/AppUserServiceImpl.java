package com.webapp.pwmanager.appUser.service;

import com.webapp.pwmanager.appUser.domain.AppUser;
import com.webapp.pwmanager.appUser.repository.AppUserRepository;
import com.webapp.pwmanager.registration.token.ConfirmationToken;
import com.webapp.pwmanager.registration.token.ConfirmationTokenService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.webapp.pwmanager.security.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class AppUserServiceImpl implements AppUserService {
    private final AppUserRepository appUserRepository;

    private final PasswordEncoder bCryptPasswordEncoder;
    private final ConfirmationTokenService confirmationTokenService;

    @Override
    public AppUser loadUserByUsername(String email) throws UsernameNotFoundException {
        return appUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("User email '%s' not found!", email)));
    }

    public boolean singUpUser(AppUser appUser, String token) {

        String encodePassword = bCryptPasswordEncoder.bCryptPasswordEncoder().encode(appUser.getPassword());

        appUser.setPassword(encodePassword);

        appUserRepository.save(appUser);

        ConfirmationToken confirmToken = new ConfirmationToken(
                token,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(15),
                appUser
        );

        confirmationTokenService.saveConfirmationToken(confirmToken);

        return true;
    }

    public void enableAppUser(String email) {
        appUserRepository.enableAppUser(email);
    }

    public boolean hasUser(String email) {
        return appUserRepository.findByEmail(email).isPresent();
    }

    public void removeAppUserNotConfirmed(String email) {
        appUserRepository.removeAppUserByEmail(email);
    }
}
