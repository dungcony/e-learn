package com.restaurant.modules.user.events;

/**
 * Cần gửi liên kết xác thực email (đăng ký mới hoặc gửi lại).
 *
 * @param rawToken token thô; chỉ tồn tại trong sự kiện và email, không bao giờ ghi log
 */
public record UserRegisteredEvent(String email, String rawToken) {
}
