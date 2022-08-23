package com.webapp.pwmanager;

import com.webapp.pwmanager.appUser.domain.AppUser;
import com.webapp.pwmanager.appUser.domain.AppUserRole;
import com.webapp.pwmanager.appUser.domain.Password;
import com.webapp.pwmanager.appUser.repository.AppUserRepository;
import com.webapp.pwmanager.appUser.repository.PasswordRepository;
import com.webapp.pwmanager.security.PasswordEncoder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@SpringBootTest
class PwmanagerApplicationTests {

    @Autowired
    AppUserRepository appUserRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    PasswordRepository passwordRepository;

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
                passwordEncoder.bCryptPasswordEncoder().encode("password"),
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
                passwordEncoder.bCryptPasswordEncoder().encode("password2"),
                "http://localhost:8080/1",
                true,
                false,
                false,
                admin,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        passwordRepository.save(password);
        passwordRepository.save(password2);

        admin.getPasswords().add(password);

        appUserRepository.save(admin);

        Assert.isTrue(admin.getId() > 0,"User saved");
        Assert.isTrue(password.getId() > 0,"Password saved");
        Set<Password> adminPasswords = admin.getPasswords();
        Assert.notEmpty(adminPasswords, "Not Empty");
    }


}
