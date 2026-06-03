package com.example.code3.service.impl;

import com.example.code3.entity.User;
import com.example.code3.repository.UserRepository;
import com.example.code3.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public User login(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(user -> password.equals(user.getPassword()))
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
