package com.restaurant.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.*;

/**
 * Sinh/verify JWT access token bằng API jjwt 0.13.x (Jwts.parser()/.verifyWith(key) —
 * KHÔNG dùng parserBuilder()/setSigningKey() đã lỗi thời của 0.11.x).
 * <p>
 * Access token chỉ chứa sub (user id), authorities (vd ROLE_CASHIER) và exp — không có trường
 * nhạy cảm khác (password, email...). Mọi claim mới phải thêm qua tham số riêng của
 * generateAccessToken (không nhét tuỳ tiện vào Map) để buộc thay đổi sau này phải sửa method
 * signature, dễ review.
 */
@Service
public class JwtService {

    private static final MacAlgorithm ALG = Jwts.SIG.HS256;

    private final SecretKey key;
    /**
     * -- GETTER --
     * AUTH-08: dùng để trả đúng
     * trong response register/login/refresh — tránh
     * hằng số trùng lặp lệch khỏi cấu hình thật
     * .
     */
    @Getter
    private final long accessTokenExpirySeconds;

    public JwtService(
            @Value("${jwt.secret}") String base64Secret,
            @Value("${jwt.access-token-expiry-seconds:3600}") long accessTokenExpirySeconds) {
        // D-01: JWT_SECRET bắt buộc qua application.yml -> ${JWT_SECRET}, không default ở đây
        // cho giá trị secret. Nếu secret không đủ độ dài base64 cho HS256 (>=32 byte sau decode),
        // Keys.hmacShaKeyFor ném WeakKeyException ngay lúc khởi động bean — hành vi ĐÚNG
        // (fail-fast, T-02-03), không try/catch nuốt lỗi.
        this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(base64Secret));
        this.accessTokenExpirySeconds = accessTokenExpirySeconds;
    }

    // Token chỉ mang sub và authorities (vd ROLE_CASHIER).
    public String generateAccessToken(UUID userId, Collection<String> authorities) {
        Instant now = Instant.now();
        var builder = Jwts.builder().subject(userId.toString());
        if (authorities != null && !authorities.isEmpty()) {
            builder.claim("authorities", authorities);
        }
        return builder
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTokenExpirySeconds)))
                .signWith(key, ALG)
                .compact();
    }

    public Claims parseAndValidate(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
