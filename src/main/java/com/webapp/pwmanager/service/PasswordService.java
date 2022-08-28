package com.webapp.pwmanager.service;

import com.webapp.pwmanager.domain.AppUser;
import com.webapp.pwmanager.domain.Password;
import com.webapp.pwmanager.dto.PasswordDTO;
import com.webapp.pwmanager.repository.AppUserRepository;
import com.webapp.pwmanager.repository.PasswordRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class PasswordService {

    private final PasswordRepository passwordRepository;
    private final AppUserRepository appUserRepository;

    public PasswordService(final PasswordRepository passwordRepository,
                           final AppUserRepository appUserRepository) {
        this.passwordRepository = passwordRepository;
        this.appUserRepository = appUserRepository;
    }

    public List<PasswordDTO> findAll() {
        return passwordRepository.findAll(Sort.by("id"))
                .stream()
                .map(password -> mapToDTO(password, new PasswordDTO()))
                .collect(Collectors.toList());
    }

    public PasswordDTO get(final Long id) {
        return passwordRepository.findById(id)
                .map(password -> mapToDTO(password, new PasswordDTO()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public Long create(final PasswordDTO passwordDTO) {
        final Password password = new Password();
        mapToEntity(passwordDTO, password);
        return passwordRepository.save(password).getId();
    }

    public void update(final Long id, final PasswordDTO passwordDTO) {
        final Password password = passwordRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        mapToEntity(passwordDTO, password);
        passwordRepository.save(password);
    }

    public void delete(final Long id) {
        passwordRepository.deleteById(id);
    }

    private PasswordDTO mapToDTO(final Password password, final PasswordDTO passwordDTO) {
        passwordDTO.setId(password.getId());
        passwordDTO.setValue(password.getValue());
        passwordDTO.setHash(password.getHash());
        passwordDTO.setWebsiteUrl(password.getWebsiteUrl());
        passwordDTO.setWeak(password.getWeak());
        passwordDTO.setFavorite(password.getFavorite());
        passwordDTO.setDuplicated(password.getDuplicated());
        passwordDTO.setUserId(password.getUser() == null ? null : password.getUser().getId());
        return passwordDTO;
    }

    private Password mapToEntity(final PasswordDTO passwordDTO, final Password password) {
        password.setValue(passwordDTO.getValue());
        password.setHash(passwordDTO.getHash());
        password.setWebsiteUrl(passwordDTO.getWebsiteUrl());
        password.setWeak(passwordDTO.getWeak());
        password.setFavorite(passwordDTO.getFavorite());
        password.setDuplicated(passwordDTO.getDuplicated());
        final AppUser user = passwordDTO.getUserId() == null ? null : appUserRepository.findById(passwordDTO.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
        password.setUser(user);
        assert user != null;
        user.getPasswords().add(password);
        appUserRepository.save(user);
        return password;
    }

    public Object countWeakPasswords() {
        Long count =  passwordRepository.countAllByWeak(true);
        return count != null ? ResponseEntity.ok(count) : ResponseEntity.noContent().build();
    }

    public Object countOutdatedPasswords() {
        List<Password> allPasswords = passwordRepository.findAll();
        Set<Object> validated = allPasswords.stream().map(this::outdatedValidator).filter(Objects::nonNull).collect(Collectors.toSet());
        return ResponseEntity.ok(validated.size());
    }

    public Object countDuplicatedPasswords() {
        //TODO: RETURNING WRONG VALUE
        Long count = passwordRepository.countAllByDuplicated(true);
        return count != null ? ResponseEntity.ok(count) : ResponseEntity.noContent().build();
    }

    public Object findAllFavoritePasswords() {
        Set<Password> results = passwordRepository.findAllByFavorite(true);
        return results != null ? ResponseEntity.ok(results) : ResponseEntity.noContent().build();
    }

    private Object outdatedValidator(Password password) {
        Duration duration = Duration.between(password.getUpdatedAt(), password.getCreatedAt());
        long diff = Math.abs(duration.toDays());
        return diff < 60 ? password : null;
    }
}
