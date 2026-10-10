package com.restaurant.modules.user.helper;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Sinh token một lần gửi qua liên kết email và băm nó để lưu. Token thô 32 byte ngẫu nhiên mã hóa Base64URL; CSDL chỉ giữ
 * băm SHA-256 nên lộ bảng {@code user_tokens} cũng không dùng lại được token.
 */
@Component
public class UserTokenHelper {

    private static final int TOKEN_BYTES = 32;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * @param raw  token thô, chỉ có trong email gửi cho người dùng
     * @param hash SHA-256 dạng hex của {@code raw}, giá trị lưu vào CSDL
     */
    public record GeneratedToken(String raw, String hash) {
    }

    public GeneratedToken generate() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return new GeneratedToken(raw, hash(raw));
    }

    public String hash(String raw) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 bắt buộc có trong mọi JVM nên nhánh này không xảy ra
            throw new IllegalStateException("SHA-256 không khả dụng", e);
        }
    }
}
