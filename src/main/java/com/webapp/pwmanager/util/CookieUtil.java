package com.webapp.pwmanager.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {
    @Value("${jwt.auth.access_token_cookie_name}")
    private String accessTokenCookieName;

    @Value("${jwt.auth.refresh_token_cookie_name}")
    private String refreshTokenCookieName;
    private final SecurityCipher securityCipher;

    public CookieUtil(SecurityCipher securityCipher) {
        this.securityCipher = securityCipher;
    }

    public HttpCookie createAccessTokenCookie(String token, Long duration) {
        return ResponseCookie.from(accessTokenCookieName, token)
                .maxAge(duration)
                .httpOnly(true)
                .path("/")
                .build();
    }

    public HttpCookie createRefreshTokenCookie(String token, Long duration) {
        return ResponseCookie.from(refreshTokenCookieName, token)
                .maxAge(duration)
                .httpOnly(true)
                .path("/")
                .build();
    }

    public HttpCookie deleteRefreshTokenCookie() {
        return ResponseCookie.from(refreshTokenCookieName, null)
                .maxAge(0)
                .httpOnly(true)
                .path("/")
                .build();
    }

    public HttpCookie deleteAccessTokenCookie() {
        return ResponseCookie.from(accessTokenCookieName, "").maxAge(0).httpOnly(true).path("/").build();
    }

}
