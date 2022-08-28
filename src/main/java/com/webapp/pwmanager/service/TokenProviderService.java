package com.webapp.pwmanager.service;

import com.webapp.pwmanager.domain.AppUser;
import com.webapp.pwmanager.domain.RefreshToken;
import com.webapp.pwmanager.domain.Token;
import com.webapp.pwmanager.dto.LogoutRequest;
import com.webapp.pwmanager.dto.TokenRefreshRequest;
import com.webapp.pwmanager.exception.TokenRefreshException;
import com.webapp.pwmanager.repository.RefreshTokenRepository;
import com.webapp.pwmanager.util.JWTTokenHelper;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Value("${jwt.auth.secret_key}")
    private String tokenSecret;

    @Value("${jwt.auth.refresh_expires_in}")
    private int refreshExpiresIn;
    @Value("${jwt.auth.expires_in}")
    private int expiresIn;

    private final RefreshTokenRepository refreshTokenRepository;

    private final JWTTokenHelper jWTTokenHelper;

    public Token generateAccessToken(String subject) throws InvalidKeySpecException, NoSuchAlgorithmException {
        Date expiryDate = jWTTokenHelper.generateExpirationDate();
        Long duration = new Date().getTime() + expiresIn * 1000L;
        String token = jWTTokenHelper.generateToken(subject);
        return new Token(Token.TokenType.ACCESS, token ,duration, LocalDateTime.ofInstant(expiryDate.toInstant(), ZoneId.systemDefault()));
    }

    public Token generateRefreshToken(AppUser subject) {
        Date expiryDate = jWTTokenHelper.generateRefreshExpirationDate();
        Long duration = new Date().getTime() + refreshExpiresIn * 1000L;
        String token = createRefreshToken(subject);
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

    @Transactional
    public String createRefreshToken(AppUser user) {
        boolean exists = refreshTokenRepository.existsByUser(user);
        if (exists) {
            refreshTokenRepository.deleteByUser(user);
        }
        RefreshToken newRefreshToken = new RefreshToken(user);
        refreshTokenRepository.save(newRefreshToken);

        return jWTTokenHelper.generateRefreshToken(user, newRefreshToken);
    }

    public Map<String, Token> validateRefreshToken(String accessToken, String refreshToken, AppUser user ) throws InvalidKeySpecException, NoSuchAlgorithmException {
        if (jWTTokenHelper.validateRefreshToken(refreshToken, user)) {
            Integer id = (Integer) jWTTokenHelper.getRefreshTokenClaim(refreshToken);
            Long refreshTokenId = id != null ? Long.valueOf(id) : null;
            assert refreshTokenId != null;
            if (refreshTokenRepository.existsById(refreshTokenId)) {

                RefreshToken oldRefreshToken = refreshTokenRepository.findById(refreshTokenId).orElse(null);

                if (oldRefreshToken != null && (Objects.equals(oldRefreshToken.getUser().getId(), user.getId()))) {
                    refreshTokenRepository.deleteById(refreshTokenId);
                    Token newAccessToken = generateAccessToken(user.getUsername());
                    Token newRefreshToken = generateRefreshToken(user);

                    return Map.of("accessToken",newAccessToken,"refreshToken",newRefreshToken);
                }
            }
        }
        throw new TokenRefreshException(refreshToken, "Refresh token is not in database!");
    }

    public ResponseEntity<?> deleteRefreshTokenByUserName(AppUser user) {
        refreshTokenRepository.deleteByUser(user);
        return refreshTokenRepository.deleteByUser(user) == 0 ? ResponseEntity.badRequest().build() : ResponseEntity.accepted().build();
    }
}
