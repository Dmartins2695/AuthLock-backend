package com.webapp.pwmanager.repository;

import com.webapp.pwmanager.domain.Password;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;


public interface PasswordRepository extends JpaRepository<Password, Long> {
    Long countAllByWeak(Boolean weak);

    Long countAllByDuplicated(Boolean duplicated);

    Set<Password> findAllByFavorite(Boolean favorite);

    Set<Password> findAllByUserId(Long userId);
}
