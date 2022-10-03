package com.webapp.pwmanager.repository;

import com.webapp.pwmanager.domain.Password;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;


public interface PasswordRepository extends JpaRepository<Password, Long> {
    Long countByUser_IdAndWeak(Long id, Boolean weak);

    Long countAllByDuplicated(Boolean duplicated);

    Set<Password> findAllByFavorite(Boolean favorite);

    Set<Password> findByUser_IdAndFavorite(Long id, Boolean favorite);

    Set<Password> findAllByUserId(Long userId);

    Long countByUser_IdAndDuplicated(Long id, Boolean duplicated);
}
