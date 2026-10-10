package com.restaurant.modules.user.events;

import com.restaurant.common.mail.EmailService;
import com.restaurant.modules.user.config.AuthProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Gửi email liên kết sau khi transaction ghi token đã commit, để không gửi liên kết cho bản ghi bị rollback.
 * Lỗi gửi chỉ ghi log WARN (không kèm liên kết) và không làm hỏng nghiệp vụ; người dùng gửi lại được.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthEmailListener {

    private final EmailService emailService;
    private final AuthProperties properties;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserRegistered(UserRegisteredEvent event) {
        try {
            emailService.sendVerificationLink(event.email(), link("/verify-email", event.rawToken()));
        } catch (RuntimeException e) {
            log.warn("Không gửi được liên kết xác thực email", e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPasswordResetRequested(PasswordResetRequestedEvent event) {
        try {
            emailService.sendPasswordResetLink(event.email(), link("/reset-password", event.rawToken()));
        } catch (RuntimeException e) {
            log.warn("Không gửi được liên kết đặt lại mật khẩu", e);
        }
    }

    private String link(String path, String rawToken) {
        String base = properties.frontendUrl();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + path + "?token=" + rawToken;
    }
}
