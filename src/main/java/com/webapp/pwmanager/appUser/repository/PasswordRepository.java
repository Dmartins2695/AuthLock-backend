package com.webapp.pwmanager.appUser.repository;

import com.webapp.pwmanager.appUser.domain.Password;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PasswordRepository extends JpaRepository<Password, Long> {
}
