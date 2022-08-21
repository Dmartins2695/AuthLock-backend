package com.webapp.pwmanager.appUser.service;

import com.webapp.pwmanager.appUser.domain.AppUser;
import com.webapp.pwmanager.appUser.domain.Password;
import com.webapp.pwmanager.appUser.model.PasswordDTO;
import com.webapp.pwmanager.appUser.repository.AppUserRepository;
import com.webapp.pwmanager.appUser.repository.PasswordRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
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

}
