package com.webapp.pwmanager.appUser.repository;

import com.webapp.pwmanager.appUser.domain.Password;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;


public interface PasswordRepository extends JpaRepository<Password, Long> {
    Long countAllByWeak(Boolean weak);

    Long countAllByDuplicated(Boolean duplicated);

    Set<Password> findAllByFavorite(Boolean favorite);
}
