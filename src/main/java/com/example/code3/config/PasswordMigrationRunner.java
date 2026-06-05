package com.example.code3.config;

import com.example.code3.entity.User;
import com.example.code3.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 密码迁移组件：将数据库中的明文密码自动迁移为 BCrypt 哈希
 * 首次启用时运行一次后即可注释 @Component 关闭
 */
@Slf4j
@Component
public class PasswordMigrationRunner implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        List<User> allUsers = userRepository.findAll();
        int migrated = 0;
        for (User user : allUsers) {
            // BCrypt 哈希以 "$2a$" 开头，明文密码不满足此格式
            if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
                log.info("迁移用户密码: username={}", user.getUsername());
                user.setPassword(passwordEncoder.encode(user.getPassword()));
                userRepository.save(user);
                migrated++;
            }
        }
        if (migrated > 0) {
            log.info("密码迁移完成，共迁移 {} 个用户", migrated);
        } else {
            log.info("无需迁移，所有密码已为 BCrypt 格式");
        }
    }
}
