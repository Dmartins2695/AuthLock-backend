package com.webapp.pwmanager;

import com.webapp.pwmanager.appUser.domain.AppUser;
import com.webapp.pwmanager.appUser.domain.AppUserRole;
import com.webapp.pwmanager.appUser.repository.AppUserRepository;
import com.webapp.pwmanager.security.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

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

		@Override
		public void run(String...args) throws Exception {
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
		}
	}
}
