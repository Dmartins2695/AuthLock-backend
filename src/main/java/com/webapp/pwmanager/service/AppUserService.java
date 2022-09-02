package com.webapp.pwmanager.service;

import com.webapp.pwmanager.domain.AppUser;
import com.webapp.pwmanager.domain.Password;
import com.webapp.pwmanager.domain.Token;
import com.webapp.pwmanager.dto.LoginResponse;
import com.webapp.pwmanager.dto.TokenRefreshResponse;
import com.webapp.pwmanager.repository.AppUserRepository;
import com.webapp.pwmanager.domain.ConfirmationToken;
import com.webapp.pwmanager.security.PasswordEncoder;
import com.webapp.pwmanager.util.CookieUtil;
import com.webapp.pwmanager.util.JWTTokenHelper;
import com.webapp.pwmanager.util.SecurityCipher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;

import java.util.Map;
import java.util.Set;


@Service
@RequiredArgsConstructor
@Slf4j
public class AppUserService implements UserDetailsService {
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder bCryptPasswordEncoder;
    private final ConfirmationTokenService confirmationTokenService;

    private final TokenProviderService tokenProviderService;

    private final CookieUtil cookieUtil;

    private final JWTTokenHelper jWTTokenHelper;
    @Autowired
    private final SecurityCipher securityCipher;

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

    public ResponseEntity<?> login(AppUser user) throws InvalidKeySpecException, NoSuchAlgorithmException {

        HttpHeaders responseHeaders = new HttpHeaders();

        Token newAccessToken = tokenProviderService.generateAccessToken(user);
        Token newRefreshToken = tokenProviderService.generateRefreshToken(user);

        addRefreshTokenCookie(responseHeaders, newRefreshToken);

        LoginResponse response = new LoginResponse();
        response.setAccessToken(securityCipher.encrypt(newAccessToken.getTokenValue()));

        log.info(String.format("User has %s logged IN", user.getEmail()));
        log.info(String.format("Token '%s' logged IN", newAccessToken.getTokenValue()));
        return ResponseEntity.ok().headers(responseHeaders).body(response);
    }


    public ResponseEntity<?> refresh( String refreshToken) throws InvalidKeySpecException, NoSuchAlgorithmException {
        String decryptedRefreshToken = securityCipher.decrypt(refreshToken);
        String username = jWTTokenHelper.getUsernameFromRefreshToken(decryptedRefreshToken);
        log.info(String.format("Token Has %s in it", username));
        if(username != null){
            AppUser user = loadUserByUsername(username);
            Map<String, Token> tokens = tokenProviderService.validateRefreshToken(decryptedRefreshToken, user);
            HttpHeaders responseHeaders = new HttpHeaders();
            addRefreshTokenCookie(responseHeaders, tokens.get("refreshToken"));
            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .body(new TokenRefreshResponse(securityCipher.encrypt(tokens.get("accessToken").getTokenValue())));
        }
        return ResponseEntity.badRequest().build();
    }

    public ResponseEntity<?> logout(String refreshToken) {
        String decryptedRefreshToken = securityCipher.decrypt(refreshToken);
        String username = jWTTokenHelper.getUsernameFromRefreshToken(decryptedRefreshToken);
        // String tokenId = jWTTokenHelper.getRefreshTokenClaim(decryptedRefreshToken);
        AppUser user = loadUserByUsername(username);
        if(tokenProviderService.deleteRefreshTokenByUser(user,decryptedRefreshToken)){
            HttpHeaders responseHeaders = new HttpHeaders();
            deleteRefreshTokenCookie(responseHeaders);
            return ResponseEntity.accepted()
                    .headers(responseHeaders).build();
        }
        return ResponseEntity.badRequest().build();
    }


    private void addAccessTokenCookie(HttpHeaders httpHeaders, Token token) {
        httpHeaders.add(HttpHeaders.SET_COOKIE, cookieUtil.createAccessTokenCookie(token.getTokenValue(), token.getDuration()).toString());
    }

    private void addRefreshTokenCookie(HttpHeaders httpHeaders, Token token ) {
        httpHeaders.add(HttpHeaders.SET_COOKIE, cookieUtil.createRefreshTokenCookie(token.getTokenValue(), token.getDuration()).toString());
    }

    private void deleteRefreshTokenCookie(HttpHeaders httpHeaders ) {
        httpHeaders.add(HttpHeaders.SET_COOKIE, cookieUtil.deleteRefreshTokenCookie().toString());
    }
}

