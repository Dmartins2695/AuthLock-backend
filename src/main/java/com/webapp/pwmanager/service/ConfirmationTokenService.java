package com.webapp.pwmanager.service;

import com.webapp.pwmanager.domain.ConfirmationToken;
import com.webapp.pwmanager.repository.ConfirmationTokenRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ConfirmationTokenService {

    private final ConfirmationTokenRepository confirmationTokenRepository;

    public void saveConfirmationToken(ConfirmationToken token) {
        confirmationTokenRepository.save(token);
    }

    public Optional<ConfirmationToken> getToken(String token) {
        return confirmationTokenRepository.findByToken(token);
    }

    public int setConfirmedAt(String token) {
        return confirmationTokenRepository.updateConfirmedAt(
                token, LocalDateTime.now());
    }

    public Optional<ConfirmationToken> getTokenByAppUserId(Long id) {
        return confirmationTokenRepository.findConfirmationTokenByAppUserId(id);
    }

    public void removeTokenByAppUserId(Long id) {
        confirmationTokenRepository.removeTokenByAppUserId(id);
    }

    public boolean tokenNotConfirmedButValid(Long id) {
        ConfirmationToken token = confirmationTokenRepository.findConfirmationTokenByAppUserId(id).get();

        return  token.getConfirmedAt() == null && token.getExpiresAt().isAfter(LocalDateTime.now());
    }
}
