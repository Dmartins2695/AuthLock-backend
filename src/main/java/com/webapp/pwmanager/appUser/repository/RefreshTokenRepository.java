package com.webapp.pwmanager.appUser.repository;


import com.webapp.pwmanager.appUser.domain.AppUser;
import com.webapp.pwmanager.appUser.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
  @Modifying
  int deleteByUser(AppUser user);

  @Modifying
  @Query(value = "SELECT token FROM refreshtoken token WHERE token = :userId")
  int deleteByUserId(@Param("userId")Long userId);

  boolean existsByUser(AppUser user);
}
