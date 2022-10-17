package com.webapp.pwmanager;

import com.webapp.pwmanager.domain.AppUser;
import com.webapp.pwmanager.domain.AppUserRole;
import com.webapp.pwmanager.domain.Password;
import com.webapp.pwmanager.repository.AppUserRepository;
import com.webapp.pwmanager.repository.PasswordRepository;
import com.webapp.pwmanager.security.PasswordEncoder;
import com.webapp.pwmanager.service.PasswordService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@ActiveProfiles("dev")
@SpringBootTest
class PwmanagerApplicationTests {

    @Autowired
    AppUserRepository appUserRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    PasswordRepository passwordRepository;

    @Autowired
    PasswordService passwordService;

    @Test
    void contextLoads() {
        Collection<SimpleGrantedAuthority> grantedAuthorities = new HashSet<>();
        grantedAuthorities.add(new SimpleGrantedAuthority(AppUserRole.ADMIN.name()));
        AppUser admin = new AppUser(
                "daniel",
                "martins",
                "daniel@gmail.com",
                passwordEncoder.bCryptPasswordEncoder().encode("password"),
                grantedAuthorities,
                true
        );

        Password password = new Password(
                "password",
                "http://localhost:8080/",
                true,
                false,
                false,
                admin,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        Password password2 = new Password(
                "password2",
                "http://localhost:8080/1",
                true,
                false,
                false,
                admin,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        appUserRepository.save(admin);

        passwordRepository.save(password);
        passwordRepository.save(password2);

        Assert.isTrue(admin.getId() > 0, "User saved");
        Assert.isTrue(password.getId() > 0, "Password saved");
        Set<Password> adminPasswords = admin.getPasswords();
        Assert.notEmpty(adminPasswords, "Not Empty");
    }

    @Test
    void DuplicatedPasswords() {
        AppUser user = appUserRepository.findByEmail("daniel@gmail.com").orElse(null);
        assert user != null;
        Password oldPassword = passwordRepository.findById(10000L).orElse(null);
        assert oldPassword != null;
        oldPassword.setDuplicated(true);
        passwordRepository.save(oldPassword);
        String password = "password1";
        updateDuplicatedMatches(password, user.getId(), oldPassword);
        oldPassword.setWeak(true);
        oldPassword.setDuplicated(isDuplicatedPassword(password, user.getId()));
        oldPassword.setValue(password);
        oldPassword.setFavorite(false);
        oldPassword.setWebsiteUrl(oldPassword.getWebsiteUrl());
        user.getPasswords().add(oldPassword);
        appUserRepository.save(user);
        passwordRepository.save(oldPassword);
        Set<Password> passwordSet = passwordRepository.findAllByUserId(user.getId());
        System.out.println(passwordSet);
    }

    private void updateDuplicatedMatches(String password, Long userId, Password oldPassword) {
        Set<Password> passwordSet = passwordRepository.findAllByUserId(userId);
        // old passwords
        List<Password> duplicatedWithOldPassword = getDuplicatedPasswordsList(passwordSet, oldPassword.getValue(), oldPassword.getId());

        if (duplicatedWithOldPassword.size() == 1) {
            duplicatedWithOldPassword.forEach(item -> {
                item.setDuplicated(false);
                passwordRepository.save(item);
            });
        }

        List<Password> duplicatedWithNewPassword = getDuplicatedPasswordsList(passwordSet, password, oldPassword.getId());

        duplicatedWithNewPassword.forEach(item -> {
            item.setDuplicated(true);
            passwordRepository.save(item);
        });
    }

    private boolean isDuplicatedPassword(String password, Long userId) {
        Set<Password> passwordSet = passwordRepository.findAllByUserId(userId);
        return passwordSet.stream().anyMatch(item -> password.equals(item.getValue()));
    }

    private List<Password> getDuplicatedPasswordsList(Set<Password> set, String s, Long id) {
        return set.stream()
                .map(item -> s.equals(item.getValue()) && !item.getId().equals(id) ? item : null).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
