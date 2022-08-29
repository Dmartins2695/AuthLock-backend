package com.webapp.pwmanager.controller;

import com.webapp.pwmanager.domain.AppUser;
import com.webapp.pwmanager.dto.AuthenticationRequest;
import com.webapp.pwmanager.dto.LogoutRequest;
import com.webapp.pwmanager.dto.TokenRefreshRequest;
import com.webapp.pwmanager.dto.UserInfoResponse;
import com.webapp.pwmanager.service.AppUserService;
import com.webapp.pwmanager.util.JWTTokenHelper;
import com.webapp.pwmanager.util.SecurityCipher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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


@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600, methods = {RequestMethod.GET, RequestMethod.POST},allowCredentials = "true")
@Slf4j
@RequiredArgsConstructor
public class AuthController {
    @Autowired
    JWTTokenHelper jWTTokenHelper;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private AppUserService appUserService;


    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthenticationRequest authenticationRequest)
            throws InvalidKeySpecException, NoSuchAlgorithmException {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(authenticationRequest.getUserName(), authenticationRequest.getPassword());

        final Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        AppUser user = (AppUser) authentication.getPrincipal();
       return appUserService.login(user);
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

        return appUserService.refresh(accessToken,decryptedRefreshToken, request);

    }

    @PostMapping("/auth/logout")
    @Transactional
    public ResponseEntity<?> logout(@Valid @RequestBody LogoutRequest request) {
        return appUserService.logout(request);
    }
}
