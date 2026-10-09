package com.elearning.user.config;

import com.elearning.user.entity.User;
import com.elearning.user.enums.Role;
import com.elearning.user.enums.UserStatus;
import com.elearning.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.UUID;

/**
 * Tạo tài khoản Quản trị viên đầu tiên lúc khởi động. SRS không có chức năng tạo QTV, mà mọi chức năng quản trị đều
 * cần một QTV, nên tài khoản này lấy từ biến môi trường thay vì nhúng mật khẩu vào migration.
 * <p>
 * Chỉ tạo khi cả hai biến cùng có giá trị và hệ thống chưa có QTV nào, nên khởi động lại không tạo trùng.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrapRunner implements ApplicationRunner {

    private final AuthProperties properties;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (!StringUtils.hasText(properties.bootstrapAdminEmail())
                || !StringUtils.hasText(properties.bootstrapAdminPassword())
                || userRepository.existsByRole(Role.ADMIN)) {
            return;
        }
        userRepository.save(User.builder()
                .id(UUID.randomUUID())
                .email(properties.bootstrapAdminEmail().trim().toLowerCase(Locale.ROOT))
                .passwordHash(passwordEncoder.encode(properties.bootstrapAdminPassword()))
                .fullName("Quản trị viên")
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .build());
        log.info("Đã tạo tài khoản quản trị viên đầu tiên: {}", properties.bootstrapAdminEmail());
    }
}
