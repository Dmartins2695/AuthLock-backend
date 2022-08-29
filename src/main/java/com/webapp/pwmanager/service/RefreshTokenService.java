package com.webapp.pwmanager.service;

import com.webapp.pwmanager.domain.AppUser;
import com.webapp.pwmanager.domain.RefreshToken;
import com.webapp.pwmanager.repository.RefreshTokenRepository;
import com.webapp.pwmanager.util.JWTTokenHelper;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@AllArgsConstructor
public class RefreshTokenService {

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


    public ResponseEntity<?> deleteRefreshTokenByUser(AppUser user) {
        refreshTokenRepository.deleteByUser(user);
        return refreshTokenRepository.deleteByUser(user) == 0 ? ResponseEntity.badRequest().build() : ResponseEntity.accepted().build();
    }

    public boolean existsById(Long refreshTokenId) {
        return refreshTokenRepository.existsById(refreshTokenId);
    }

    public Optional<RefreshToken> findById(Long refreshTokenId) {
        return refreshTokenRepository.findById(refreshTokenId);
    }

    public void deleteById(Long refreshTokenId) {
        refreshTokenRepository.deleteById(refreshTokenId);
    }
}