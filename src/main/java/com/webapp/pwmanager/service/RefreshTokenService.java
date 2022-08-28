package com.webapp.pwmanager.service;

import com.webapp.pwmanager.domain.AppUser;
import com.webapp.pwmanager.domain.RefreshToken;
import com.webapp.pwmanager.repository.RefreshTokenRepository;
import com.webapp.pwmanager.dto.LogoutRequest;
import com.webapp.pwmanager.dto.TokenRefreshRequest;
import com.webapp.pwmanager.exception.TokenRefreshException;
import com.webapp.pwmanager.util.JWTTokenHelper;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Objects;

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
