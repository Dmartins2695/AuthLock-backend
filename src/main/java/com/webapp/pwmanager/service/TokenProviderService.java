package com.webapp.pwmanager.service;

import com.webapp.pwmanager.domain.AppUser;
import com.webapp.pwmanager.domain.RefreshToken;
import com.webapp.pwmanager.domain.Token;
import com.webapp.pwmanager.exception.TokenRefreshException;
import com.webapp.pwmanager.util.JWTTokenHelper;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TokenProviderService {

    @Autowired
    private final RefreshTokenService refreshTokenService;
    private final JWTTokenHelper jWTTokenHelper;
    @Value("${jwt.auth.secret_key}")
    private String tokenSecret;
    @Value("${jwt.auth.refresh_expires_in}")
    private int refreshExpiresIn;
    @Value("${jwt.auth.expires_in}")
    private int expiresIn;

    public Token generateAccessToken(AppUser subject) throws InvalidKeySpecException, NoSuchAlgorithmException {
        Date expiryDate = jWTTokenHelper.generateExpirationDate();
        Long duration = new Date().getTime() + expiresIn * 60 * 1000L;
        String token = jWTTokenHelper.generateToken(subject);
        return new Token(Token.TokenType.ACCESS, token, duration, LocalDateTime.ofInstant(expiryDate.toInstant(), ZoneId.systemDefault()));
    }

    public Token generateRefreshToken(AppUser subject) {
        Date expiryDate = jWTTokenHelper.generateRefreshExpirationDate();
        Long duration = new Date().getTime() + refreshExpiresIn * 60 * 1000L;
        String token = refreshTokenService.createRefreshToken(subject);
        return new Token(Token.TokenType.REFRESH, token, duration, LocalDateTime.ofInstant(expiryDate.toInstant(), ZoneId.systemDefault()));
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

    public Map<String, Token> validateRefreshToken(String refreshToken, AppUser user) throws InvalidKeySpecException, NoSuchAlgorithmException {
        if (jWTTokenHelper.validateRefreshToken(refreshToken, user)) {
            Integer id = (Integer) jWTTokenHelper.getRefreshTokenClaim(refreshToken);
            Long refreshTokenId = id != null ? Long.valueOf(id) : null;
            assert refreshTokenId != null;
            if (verifyRefreshTokenUserWithUserId(refreshTokenId, user)) {
                refreshTokenService.deleteById(refreshTokenId);
                Token newAccessToken = generateAccessToken(user);
                Token newRefreshToken = generateRefreshToken(user);
                return Map.of("accessToken", newAccessToken, "refreshToken", newRefreshToken);
            }
        }
        throw new TokenRefreshException(refreshToken, "Refresh token is not in database!");
    }

    private boolean verifyRefreshTokenUserWithUserId(Long refreshTokenId, AppUser user) throws InvalidKeySpecException, NoSuchAlgorithmException {
        if (refreshTokenService.existsById(refreshTokenId)) {

            RefreshToken oldRefreshToken = refreshTokenService.findById(refreshTokenId).orElse(null);

            return oldRefreshToken != null && (Objects.equals(oldRefreshToken.getUser().getId(), user.getId()));
        }
        return false;
    }

    public boolean deleteRefreshTokenByUser(AppUser user, String refreshToken) {
        Integer id = (Integer) jWTTokenHelper.getRefreshTokenClaim(refreshToken);
        Long refreshTokenId = id != null ? Long.valueOf(id) : null;
        assert refreshTokenId != null;
        try {
            if (verifyRefreshTokenUserWithUserId(refreshTokenId, user)) {
                refreshTokenService.deleteById(refreshTokenId);
                return true;
            }
            return false;
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}

