package com.webapp.pwmanager.appUser.web;

import com.webapp.pwmanager.appUser.domain.AppUser;
import com.webapp.pwmanager.appUser.requestsDto.AuthenticationRequest;
import com.webapp.pwmanager.appUser.requestsDto.LogoutRequest;
import com.webapp.pwmanager.appUser.requestsDto.TokenRefreshRequest;
import com.webapp.pwmanager.appUser.responseDto.LoginResponse;
import com.webapp.pwmanager.appUser.responseDto.UserInfo;
import com.webapp.pwmanager.appUser.service.RefreshTokenService;
import com.webapp.pwmanager.security.jwt.JWTTokenHelper;
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
import java.util.Arrays;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/v1")
@CrossOrigin
@Slf4j
public class AuthController {
    @Autowired
    JWTTokenHelper jWTTokenHelper;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private RefreshTokenService refreshTokenService;

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthenticationRequest authenticationRequest) throws InvalidKeySpecException, NoSuchAlgorithmException {

        if (!SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(authenticationRequest.getUserName(), authenticationRequest.getPassword());

            final Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            AppUser user = (AppUser) authentication.getPrincipal();

            String accessToken = jWTTokenHelper.generateToken(user.getUsername());
            String refreshToken = refreshTokenService.createRefreshToken(user);


            LoginResponse response = new LoginResponse();
            response.setAccessToken(accessToken);
            response.setRefreshToken(refreshToken);
            response.setRoles(Arrays.stream(user.getGrantedAuthorities().stream().toArray()).map(r -> r.toString()).collect(Collectors.toList()));

            log.info(String.format("User has %s logged IN", user.getEmail()));
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body("User already logged in");
    }

    @GetMapping("/auth/userinfo")
    public ResponseEntity<?> getUserInfo(Principal user) {
        AppUser userObj = (AppUser) userDetailsService.loadUserByUsername(user.getName());

        UserInfo userInfo = new UserInfo();
        userInfo.setFirstName(userObj.getFirstName());
        userInfo.setLastName(userObj.getLastName());
        userInfo.setEmail(userObj.getEmail());
        userInfo.setRoles(userObj.getAuthorities().toArray());


        return ResponseEntity.ok(userInfo);


    }

    @PostMapping("/auth/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequest request) throws InvalidKeySpecException, NoSuchAlgorithmException {
        return refreshTokenService.validateRefreshToken(request);
    }

    @PostMapping("/auth/logout")
    @Transactional
    public ResponseEntity<?> logout(@Valid @RequestBody LogoutRequest request) {
        return refreshTokenService.deleteRefreshTokenByUserName(request);
    }
}
