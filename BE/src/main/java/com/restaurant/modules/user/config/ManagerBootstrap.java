package com.restaurant.modules.user.config;

import com.restaurant.modules.user.entity.User;
import com.restaurant.modules.user.enums.Role;
import com.restaurant.modules.user.enums.UserStatus;
import com.restaurant.modules.user.repository.UserRepository;
import com.restaurant.modules.user.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * Tạo tài khoản Quản lý đầu tiên lúc khởi động: SRS không có chức năng tạo Quản lý đầu tiên nên phải nạp từ cấu hình.
 * Chỉ chạy khi cả email lẫn mật khẩu được cấu hình và hệ thống chưa có Quản lý nào.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ManagerBootstrap implements ApplicationRunner {

    private final AuthProperties properties;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (!StringUtils.hasText(properties.bootstrapAdminEmail())
                || !StringUtils.hasText(properties.bootstrapAdminPassword())
                || userRepository.existsByRole(Role.MANAGER)) {
            return;
        }
        User manager = new User();
        manager.setId(UUID.randomUUID());
        manager.setEmail(UserValidator.normalizeEmail(properties.bootstrapAdminEmail()));
        manager.setPasswordHash(passwordEncoder.encode(properties.bootstrapAdminPassword()));
        manager.setFullName("Quản lý");
        manager.setRole(Role.MANAGER);
        manager.setStatus(UserStatus.ACTIVE);
        manager.setEmailVerified(true);
        manager.setMustChangePassword(false);
        userRepository.save(manager);
        log.info("Đã tạo tài khoản Quản lý đầu tiên từ cấu hình");
    }
}
