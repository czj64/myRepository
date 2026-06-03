package com.example.code3.repository;

import com.example.code3.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByPhone(String phone);
    long countByRole(Integer role);
    Page<User> findByUsernameContainingOrNameContaining(String username, String name, Pageable pageable);
}
