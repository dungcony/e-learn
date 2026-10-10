package com.restaurant.modules.user.dto.response;

/**
 * @param accessToken JWT HS256 gửi kèm header {@code Authorization: Bearer ...}
 * @param expiresIn   thời hạn token, đơn vị giây
 */
public record AuthResponse(String accessToken, long expiresIn, AuthUserResponse user) {
}
