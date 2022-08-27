package com.webapp.pwmanager.appUser.service;

import com.webapp.pwmanager.appUser.domain.AppUser;
import com.webapp.pwmanager.appUser.domain.Password;
import com.webapp.pwmanager.appUser.repository.AppUserRepository;
import com.webapp.pwmanager.registration.token.ConfirmationToken;
import com.webapp.pwmanager.registration.token.ConfirmationTokenService;
import com.webapp.pwmanager.security.PasswordEncoder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@AllArgsConstructor
@Slf4j
public class AppUserService implements UserDetailsService {
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder bCryptPasswordEncoder;
    private final ConfirmationTokenService confirmationTokenService;

    @Override
    public AppUser loadUserByUsername(String email) throws UsernameNotFoundException {
        return appUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("User email '%s' not found!", email)));
    }

    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String currentUserName = authentication.getName();
            return ResponseEntity.ok(appUserRepository.findByEmail(currentUserName));
        }
        return ResponseEntity.noContent().build();
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

    public ResponseEntity<?> getUserStoredPasswords(Long userId) {
        AppUser user = appUserRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException(String.format("User email '%s' not found!", userId)));
        Set<Password> allByUserId = user.getPasswords();
        log.info(String.format("User has %s logged IN", user.getEmail()));
        return allByUserId != null ? ResponseEntity.ok(allByUserId) : ResponseEntity.noContent().build();
    }
}

