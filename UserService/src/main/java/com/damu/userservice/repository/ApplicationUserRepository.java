package com.damu.userservice.repository;

import com.damu.userservice.entity.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicationUserRepository extends JpaRepository<ApplicationUser, Long> {
    Optional<ApplicationUser> findByAuthSubject(String authSubject);

    Optional<ApplicationUser> findByEmail(String email);
}
