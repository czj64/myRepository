package com.example.code3.service;

import com.example.code3.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    User login(String username, String password);
    User getById(Long id);
    User save(User user);
    long countByRole(Integer role);
    Page<User> findAll(Pageable pageable);
    boolean existsByUsername(String username);
    Page<User> searchUsers(String keyword, Pageable pageable);
}
