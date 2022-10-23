package com.webapp.pwmanager.service;

import com.webapp.pwmanager.domain.AppUser;
import com.webapp.pwmanager.domain.Password;
import com.webapp.pwmanager.dto.DataDTO;
import com.webapp.pwmanager.dto.UpdateDataDto;
import com.webapp.pwmanager.repository.AppUserRepository;
import com.webapp.pwmanager.repository.PasswordRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Service
public class PasswordService {

    public static final Pattern VALID_PASSWORD_REGEX =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?& ])[A-Za-z\\d@$!%*?& ]{24,}$");
    private final PasswordRepository passwordRepository;
    private final AppUserRepository appUserRepository;

    public PasswordService(final PasswordRepository passwordRepository,
                           final AppUserRepository appUserRepository) {
        this.passwordRepository = passwordRepository;
        this.appUserRepository = appUserRepository;
    }

    public Set<Password> findAllByUser(Long userId) {
        return passwordRepository.findAllByUserId(userId);
    }

    public Password create(AppUser user, UpdateDataDto data) {
        if (!data.getPassword().isEmpty() && !data.getWebsiteUrl().isEmpty()) {
            final Password newPassword = new Password();
            newPassword.setUser(user);
            newPassword.setValue(data.getPassword());
            newPassword.setWebsiteUrl(data.getWebsiteUrl());
            newPassword.setFavorite(false);
            newPassword.setWeak(isWeakPassword(data.getPassword()));
            newPassword.setDuplicated(isDuplicatedPassword(data.getPassword(), user.getId()));
            updateDuplicatedMatches(data.getPassword(), user.getId(), newPassword);
            passwordRepository.save(newPassword);
            return newPassword;
        }
        return null;
    }

    public Password update(final Long userId, final Password oldPassword, UpdateDataDto data) {
        if (oldPassword != null) {
            updateDuplicatedMatches(data.getPassword(), userId, oldPassword);
            oldPassword.setWeak(isWeakPassword(data.getPassword()));
            oldPassword.setDuplicated(isDuplicatedPassword(data.getPassword(), userId));
            oldPassword.setValue(data.getPassword());
            oldPassword.setFavorite(false);
            oldPassword.setWebsiteUrl(data.getWebsiteUrl());
            mapToEntity(oldPassword);
            passwordRepository.save(oldPassword);
        }
        return oldPassword;
    }

    private void updateDuplicatedMatches(String password, Long userId, Password oldPassword) {
        Set<Password> passwordSet = passwordRepository.findAllByUserId(userId);
        // old passwords
        matchWithOldPassword(userId, oldPassword, passwordSet);

        matchWithNewPassword(userId, password, oldPassword.getId(), passwordSet);
    }

    private void matchWithOldPassword(Long userId, Password oldPassword, Set<Password> passwordSet) {
        List<Password> duplicatedWithOldPassword = getDuplicatedPasswordsList(passwordSet, oldPassword.getValue(), oldPassword.getId());

        if (duplicatedWithOldPassword.size() == 1) {
            duplicatedWithOldPassword.forEach(item -> {
                item.setDuplicated(false);
                passwordRepository.save(item);
            });
        }
    }

    private void matchWithNewPassword(Long userId, String password, Long passwordId, Set<Password> passwordSet) {
        List<Password> duplicatedWithNewPassword = getDuplicatedPasswordsList(passwordSet, password, passwordId);

        duplicatedWithNewPassword.forEach(item -> {
            item.setDuplicated(true);
            passwordRepository.save(item);
        });
    }

    private List<Password> getDuplicatedPasswordsList(Set<Password> set, String s, Long id) {
        return set.stream()
                .map(item -> s.equals(item.getValue()) && !item.getId().equals(id) ? item : null).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private boolean isDuplicatedPassword(String password, Long userId) {
        Set<Password> passwordSet = passwordRepository.findAllByUserId(userId);
        return passwordSet.stream().anyMatch(item -> password.equals(item.getValue()));
    }

    private boolean isWeakPassword(String password) {
        return !VALID_PASSWORD_REGEX.matcher(password).matches();
    }

    public void delete(final Long id) {
        passwordRepository.deleteById(id);
    }

    private void mapToEntity(final Password password) {
        final AppUser user = password.getUser().getId() == null ? null : appUserRepository.findById(password.getUser().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
        password.setUser(user);
        assert user != null;
        user.getPasswords().add(password);
        appUserRepository.save(user);
    }

    public ResponseEntity<?> countWeakPasswords(Long userId) {
        DataDTO data = new DataDTO(passwordRepository.countByUser_IdAndWeak(userId, true));
        return ResponseEntity.ok(data);
    }

    public ResponseEntity<?> countOutdatedPasswords(Long userId) {
        Set<Password> allPasswords = passwordRepository.findAllByUserId(userId);
        Set<Object> outdated = allPasswords.stream().map(this::outdatedValidator).filter(Objects::nonNull).collect(Collectors.toSet());
        DataDTO data = new DataDTO((long) outdated.size());
        return ResponseEntity.ok(data);
    }

    public ResponseEntity<?> countDuplicatedPasswords(Long userId) {
        Long count = passwordRepository.countByUser_IdAndDuplicated(userId, true);
        DataDTO data = new DataDTO(count);
        return ResponseEntity.ok(data);
    }

    public ResponseEntity<?> findAllFavoritePasswords(Long userId) {
        Set<Password> results = passwordRepository.findByUser_IdAndFavorite(userId, true);
        DataDTO data = new DataDTO(results);
        return ResponseEntity.ok(data);
    }

    private Object outdatedValidator(Password password) {
        Duration duration = Duration.between(password.getUpdatedAt(), password.getCreatedAt());
        long diff = Math.abs(duration.toDays());
        return diff > 60 ? password : null;
    }

    public Optional<Password> findById(Long id) {
        return passwordRepository.findById(id);
    }

    public Password setFavorite(AppUser user, Long id) {
        Password password = passwordRepository.findById(id).orElse(null);
        assert password != null;
        password.setFavorite(!password.getFavorite());
        passwordRepository.save(password);
        return password;
    }
}

