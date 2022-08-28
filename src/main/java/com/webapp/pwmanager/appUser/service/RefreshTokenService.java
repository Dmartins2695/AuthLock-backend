package com.webapp.pwmanager.appUser.service;

import com.webapp.pwmanager.appUser.domain.AppUser;
import com.webapp.pwmanager.appUser.domain.RefreshToken;
import com.webapp.pwmanager.appUser.repository.RefreshTokenRepository;
import com.webapp.pwmanager.appUser.requestsDto.LogoutRequest;
import com.webapp.pwmanager.appUser.requestsDto.TokenRefreshRequest;
import com.webapp.pwmanager.appUser.responseDto.TokenRefreshResponse;
import com.webapp.pwmanager.exception.TokenRefreshException;
import com.webapp.pwmanager.jwt.JWTTokenHelper;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
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

    public ResponseEntity<?> validateRefreshToken(TokenRefreshRequest request) throws InvalidKeySpecException, NoSuchAlgorithmException {
        String requestRefreshToken = request.getRefreshToken();
        Integer id = (Integer) jWTTokenHelper.getRefreshTokenClaim(request.getRefreshToken());
        Long refreshTokenId = id != null ? Long.valueOf(id) : null;
        if (jWTTokenHelper.validateRefreshToken(requestRefreshToken, appUserService.loadUserByUsername(request.getUserName()))) {
            assert refreshTokenId != null;
            if (refreshTokenRepository.existsById(refreshTokenId)) {

                AppUser user = appUserService.loadUserByUsername(jWTTokenHelper.getUsernameFromRefreshToken(requestRefreshToken));

                RefreshToken oldRefreshToken = refreshTokenRepository.findById(refreshTokenId).orElse(null);

                if (oldRefreshToken != null && (Objects.equals(oldRefreshToken.getUser().getId(), user.getId()))) {
                    refreshTokenRepository.deleteById(refreshTokenId);

                    String newRefreshToken = createRefreshToken(user);
                    String accessToken = jWTTokenHelper.generateToken(user.getUsername());
                    return ResponseEntity.ok(new TokenRefreshResponse(
                            accessToken,
                            newRefreshToken,
                            Arrays.stream(user.getGrantedAuthorities().toArray()).map(Object::toString).collect(Collectors.toList()))
                    );
                }
            }
        }
        throw new TokenRefreshException(requestRefreshToken, "Refresh token is not in database!");
    }

    public ResponseEntity<?> deleteRefreshTokenByUserName(LogoutRequest request) {
        AppUser user = appUserService.loadUserByUsername(request.getUserName());
        refreshTokenRepository.deleteByUser(user);
        return refreshTokenRepository.deleteByUser(user) == 0 ? ResponseEntity.badRequest().build() : ResponseEntity.accepted().build();
    }
}
