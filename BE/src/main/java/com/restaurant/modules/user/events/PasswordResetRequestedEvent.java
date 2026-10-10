package com.restaurant.modules.user.events;

/**
 * Cần gửi liên kết đặt lại mật khẩu.
 *
 * @param rawToken token thô; chỉ tồn tại trong sự kiện và email, không bao giờ ghi log
 */
public record PasswordResetRequestedEvent(String email, String rawToken) {
}
