package com.webapp.pwmanager.appUser.service;

import com.webapp.pwmanager.appUser.domain.AppUser;
import com.webapp.pwmanager.appUser.domain.RefreshToken;
import com.webapp.pwmanager.appUser.repository.RefreshTokenRepository;
import com.webapp.pwmanager.appUser.requestsDto.LogoutRequest;
import com.webapp.pwmanager.appUser.requestsDto.TokenRefreshRequest;
import com.webapp.pwmanager.appUser.responseDto.Token;
import com.webapp.pwmanager.appUser.responseDto.TokenRefreshResponse;
import com.webapp.pwmanager.exception.TokenRefreshException;
import com.webapp.pwmanager.jwt.JWTTokenHelper;
import com.webapp.pwmanager.util.CookieUtil;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RefreshTokenService {

    private final AppUserService appUserService;

    private final RefreshTokenRepository refreshTokenRepository;

    private final JWTTokenHelper jWTTokenHelper;

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

    public AppUser validateRefreshToken(String accessToken, String refreshToken,TokenRefreshRequest request ) throws InvalidKeySpecException, NoSuchAlgorithmException {
        if (jWTTokenHelper.validateRefreshToken(refreshToken, appUserService.loadUserByUsername(request.getUserName()))) {
            Integer id = (Integer) jWTTokenHelper.getRefreshTokenClaim(refreshToken);
            Long refreshTokenId = id != null ? Long.valueOf(id) : null;
            assert refreshTokenId != null;
            if (refreshTokenRepository.existsById(refreshTokenId)) {

                AppUser user = appUserService.loadUserByUsername(jWTTokenHelper.getUsernameFromRefreshToken(refreshToken));

                RefreshToken oldRefreshToken = refreshTokenRepository.findById(refreshTokenId).orElse(null);

                if (oldRefreshToken != null && (Objects.equals(oldRefreshToken.getUser().getId(), user.getId()))) {
                    refreshTokenRepository.deleteById(refreshTokenId);

                    return user;
                }
            }
        }
        throw new TokenRefreshException(refreshToken, "Refresh token is not in database!");
    }

    public ResponseEntity<?> deleteRefreshTokenByUserName(LogoutRequest request) {
        AppUser user = appUserService.loadUserByUsername(request.getUserName());
        refreshTokenRepository.deleteByUser(user);
        return refreshTokenRepository.deleteByUser(user) == 0 ? ResponseEntity.badRequest().build() : ResponseEntity.accepted().build();
    }
}
