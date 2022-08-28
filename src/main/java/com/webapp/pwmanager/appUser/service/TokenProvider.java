package com.webapp.pwmanager.appUser.service;

import com.webapp.pwmanager.appUser.domain.AppUser;
import com.webapp.pwmanager.appUser.responseDto.Token;
import com.webapp.pwmanager.jwt.JWTTokenHelper;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
public class TokenProvider {

    @Value("${jwt.auth.secret_key}")
    private String tokenSecret;

    @Value("${jwt.auth.refresh_expires_in}")
    private int refreshExpiresIn;
    @Value("${jwt.auth.expires_in}")
    private int expiresIn;

    @Autowired
    JWTTokenHelper jwtTokenHelper;
    @Autowired
    RefreshTokenService refreshTokenService;

    public Token generateAccessToken(String subject) throws InvalidKeySpecException, NoSuchAlgorithmException {
        Date expiryDate = jwtTokenHelper.generateExpirationDate();
        Long duration = new Date().getTime() + expiresIn * 1000L;
        String token = jwtTokenHelper.generateToken(subject);
        return new Token(Token.TokenType.ACCESS, token ,duration, LocalDateTime.ofInstant(expiryDate.toInstant(), ZoneId.systemDefault()));
    }

    public Token generateRefreshToken(AppUser subject) {
        Date expiryDate = jwtTokenHelper.generateRefreshExpirationDate();
        Long duration = new Date().getTime() + refreshExpiresIn * 1000L;
        String token = refreshTokenService.createRefreshToken(subject);
        return new Token(Token.TokenType.REFRESH, token,duration, LocalDateTime.ofInstant(expiryDate.toInstant(), ZoneId.systemDefault()));
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser().setSigningKey(tokenSecret).parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    public LocalDateTime getExpiryDateFromToken(String token) {
        Claims claims = Jwts.parser().setSigningKey(tokenSecret).parseClaimsJws(token).getBody();
        return LocalDateTime.ofInstant(claims.getExpiration().toInstant(), ZoneId.systemDefault());
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(tokenSecret).parse(token);
            return true;
        } catch (SignatureException | MalformedJwtException | ExpiredJwtException | UnsupportedJwtException |
                 IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }
}

