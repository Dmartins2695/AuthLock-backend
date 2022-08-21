package com.webapp.pwmanager;

import com.webapp.pwmanager.appUser.domain.AppUser;
import com.webapp.pwmanager.appUser.domain.AppUserRole;
import com.webapp.pwmanager.appUser.domain.Password;
import com.webapp.pwmanager.appUser.repository.AppUserRepository;
import com.webapp.pwmanager.appUser.repository.PasswordRepository;
import com.webapp.pwmanager.security.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;

@SpringBootApplication
public class PwmanagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(PwmanagerApplication.class, args);
    }

    @Component
    public static class CommandLineAppStartupRunner implements CommandLineRunner {
        @Autowired
        AppUserRepository appUserRepository;
        @Autowired
        PasswordEncoder passwordEncoder;
        @Autowired
        PasswordRepository passwordRepository;

        @Override
        public void run(String... args) throws Exception {
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

            appUserRepository.save(admin);

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

        }
    }
}
