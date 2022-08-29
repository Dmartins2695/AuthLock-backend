package com.webapp.pwmanager;

import com.webapp.pwmanager.domain.AppUser;
import com.webapp.pwmanager.domain.AppUserRole;
import com.webapp.pwmanager.domain.Password;
import com.webapp.pwmanager.repository.AppUserRepository;
import com.webapp.pwmanager.repository.PasswordRepository;
import com.webapp.pwmanager.security.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;

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
            Collection<SimpleGrantedAuthority> authorities = AppUserRole.ADMIN.getGrantedAuthorities();
            Collection<SimpleGrantedAuthority> authorities2 = AppUserRole.USER.getGrantedAuthorities();
            AppUser admin = new AppUser(
                    "daniel",
                    "martins",
                    "daniel@gmail.com",
                    passwordEncoder.bCryptPasswordEncoder().encode("password"),
                    authorities,
                    true
            );
            AppUser user = new AppUser(
                    "daniel",
                    "martins",
                    "daniel1@gmail.com",
                    passwordEncoder.bCryptPasswordEncoder().encode("password"),
                    authorities2,
                    true
            );

            appUserRepository.save(admin);
            appUserRepository.save(user);

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
            Password password3 = new Password(
                    "password3",
                    passwordEncoder.bCryptPasswordEncoder().encode("password2"),
                    "http://localhost:8080/1",
                    true,
                    false,
                    false,
                    user,
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            passwordRepository.save(password);
            passwordRepository.save(password2);
            passwordRepository.save(password3);

        }
    }
}
