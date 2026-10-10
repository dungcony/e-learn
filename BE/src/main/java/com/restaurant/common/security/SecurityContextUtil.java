package com.restaurant.common.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

/**
 * Helper lấy userId hiện tại từ SecurityContextHolder — dùng lại ở mọi Service/Aspect
 * cần biết "current user" (CORE-05: kiểm tra quyền ngay trong câu truy vấn).
 */
public final class SecurityContextUtil {

    private SecurityContextUtil() {
    }

    public static UUID currentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return UUID.fromString(principal.toString());
    }

    /**
     * Id người dùng hiện tại, hoặc {@code null} nếu request chưa đăng nhập (Khách) — dùng cho endpoint công khai
     * có hành vi khác khi đã đăng nhập.
     */
    public static UUID currentUserIdOrNull() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return UUID.fromString(authentication.getPrincipal().toString());
    }

    // Người dùng hiện tại có role này không, theo claim authorities trong token ("ROLE_" + tên role).
    public static boolean hasRole(String role) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }
}
