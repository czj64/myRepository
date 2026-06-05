package com.example.code3.service.impl;

import com.example.code3.entity.User;
import com.example.code3.repository.UserRepository;
import com.example.code3.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public User login(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(user -> {
                    // BCrypt 验证
                    try {
                        if (passwordEncoder.matches(password, user.getPassword())) {
                            return true;
                        }
                    } catch (Exception ignored) {}
                    // 明文 fallback：旧密码兼容，验证通过后自动迁移
                    if (password.equals(user.getPassword()) && !user.getPassword().startsWith("$2a$")) {
                        user.setPassword(passwordEncoder.encode(password));
                        userRepository.save(user);
                        return true;
                    }
                    return false;
                })
                .orElse(null);
    }

    @Override
    public User getById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public boolean checkPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    @Override
    public void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public long countByRole(Integer role) {
        return userRepository.countByRole(role);
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    @Override
    public Page<User> searchUsers(String keyword, Pageable pageable) {
        return userRepository.findByUsernameContainingOrNameContaining(keyword, keyword, pageable);
    }
}
