package com.wisewealth.repository;

import com.wisewealth.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findByVerificationToken(String verificationToken);
    boolean existsByEmail(String email);
    Page<User> findByIsActive(Boolean isActive, Pageable pageable);
    Page<User> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
