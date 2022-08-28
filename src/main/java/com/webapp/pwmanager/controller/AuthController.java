package com.webapp.pwmanager.controller;

import com.webapp.pwmanager.domain.AppUser;
import com.webapp.pwmanager.dto.AuthenticationRequest;
import com.webapp.pwmanager.dto.LogoutRequest;
import com.webapp.pwmanager.dto.TokenRefreshRequest;
import com.webapp.pwmanager.dto.LoginResponse;
import com.webapp.pwmanager.domain.Token;
import com.webapp.pwmanager.dto.TokenRefreshResponse;
import com.webapp.pwmanager.dto.UserInfoResponse;
import com.webapp.pwmanager.service.AppUserService;
import com.webapp.pwmanager.service.RefreshTokenService;
import com.webapp.pwmanager.service.TokenProvider;
import com.webapp.pwmanager.util.JWTTokenHelper;
import com.webapp.pwmanager.util.CookieUtil;
import com.webapp.pwmanager.util.SecurityCipher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600, methods = {RequestMethod.GET, RequestMethod.POST},allowCredentials = "true")
@Slf4j
public class AuthController {
    @Autowired
    JWTTokenHelper jWTTokenHelper;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserDetailsService userDetailsService;

    private AppUserService appUserService;
    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private CookieUtil cookieUtil;

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthenticationRequest authenticationRequest)
            throws InvalidKeySpecException, NoSuchAlgorithmException {

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(authenticationRequest.getUserName(), authenticationRequest.getPassword());

        final Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        AppUser user = (AppUser) authentication.getPrincipal();
        HttpHeaders responseHeaders = new HttpHeaders();
        Token newAccessToken = tokenProvider.generateAccessToken(user.getUsername());
        Token newRefreshToken = tokenProvider.generateRefreshToken(user);
        addAccessTokenCookie(responseHeaders, newAccessToken);
        addRefreshTokenCookie(responseHeaders, newRefreshToken);
        LoginResponse response = new LoginResponse();
        response.setAccessToken("null");
        response.setRefreshToken("null");
        response.setRoles(Arrays.stream(user.getGrantedAuthorities().toArray()).map(Object::toString).collect(Collectors.toList()));
        log.info(String.format("User has %s logged IN", user.getEmail()));
        log.info(String.format("Refresh Token '%s' logged IN", newRefreshToken.getTokenValue()));
        String encryptedToken = SecurityCipher.encrypt(newRefreshToken.getTokenValue());
        log.info(String.format("Encrypted Token '%s' logged IN", encryptedToken));
        return ResponseEntity.ok().headers(responseHeaders).body(response);
    }


    @GetMapping("/auth/userinfo")
    public ResponseEntity<?> getUserInfo(Principal user) {
        AppUser userObj = (AppUser) userDetailsService.loadUserByUsername(user.getName());

        UserInfoResponse userInfo = new UserInfoResponse();
        userInfo.setFirstName(userObj.getFirstName());
        userInfo.setLastName(userObj.getLastName());
        userInfo.setEmail(userObj.getEmail());
        userInfo.setRoles(userObj.getAuthorities().toArray());


        return ResponseEntity.ok(userInfo);


    }

    @PostMapping("/auth/refresh-token")
    public ResponseEntity<?> refreshToken(@CookieValue(name = "accessToken", required = false) String accessToken,
                                          @CookieValue(name = "refreshToken", required = false) String refreshToken, @RequestBody TokenRefreshRequest request) throws InvalidKeySpecException, NoSuchAlgorithmException {
        String decryptedRefreshToken = SecurityCipher.decrypt(refreshToken);

        AppUser user= refreshTokenService.validateRefreshToken(accessToken,decryptedRefreshToken, request);
        HttpHeaders responseHeaders = new HttpHeaders();
        Token newAccessToken = tokenProvider.generateAccessToken(user.getUsername());
        Token newRefreshToken = tokenProvider.generateRefreshToken(user);
        addAccessTokenCookie(responseHeaders, newAccessToken);
        addRefreshTokenCookie(responseHeaders, newRefreshToken);
        return ResponseEntity.ok().headers(responseHeaders).body(new TokenRefreshResponse(
                "null",
                "null",
                Arrays.stream(user.getGrantedAuthorities().toArray()).map(Object::toString).collect(Collectors.toList()))
        );

    }

    @PostMapping("/auth/logout")
    @Transactional
    public ResponseEntity<?> logout(@Valid @RequestBody LogoutRequest request) {
        return refreshTokenService.deleteRefreshTokenByUserName(request);
    }

    private void addAccessTokenCookie(HttpHeaders httpHeaders, Token token) {
        httpHeaders.add(HttpHeaders.SET_COOKIE, cookieUtil.createAccessTokenCookie(token.getTokenValue(), token.getDuration()).toString());
    }

    private void addRefreshTokenCookie(HttpHeaders httpHeaders, Token token) {
        httpHeaders.add(HttpHeaders.SET_COOKIE, cookieUtil.createRefreshTokenCookie(token.getTokenValue(), token.getDuration()).toString());
    }
}
