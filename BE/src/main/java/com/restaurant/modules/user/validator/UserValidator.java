package com.restaurant.modules.user.validator;

import com.restaurant.common.exception.BusinessException;
import com.restaurant.common.exception.ErrorCode;
import com.restaurant.modules.user.enums.Role;
import com.restaurant.modules.user.repository.UserRepository;
import com.restaurant.modules.user.service.UserDeletionGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.UUID;

/** Kiểm tra nghiệp vụ của tài khoản cần tới CSDL hoặc nhiều trường (rule 2.6, tầng 2). */
@Component
@RequiredArgsConstructor
public class UserValidator {

    private final UserRepository userRepository;
    private final ObjectProvider<UserDeletionGuard> deletionGuards;

    /** Email lưu và tra cứu luôn ở dạng chữ thường, không khoảng trắng đầu/cuối. */
    public static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * @param email         email đã chuẩn hóa
     * @param excludeUserId tài khoản đang sửa (bỏ qua chính nó); {@code null} khi tạo mới
     * @throws BusinessException {@code AUTH_EMAIL_ALREADY_EXISTS} nếu email thuộc tài khoản khác chưa xóa
     */
    public void validateEmailNotTaken(String email, UUID excludeUserId) {
        boolean taken = excludeUserId == null
                ? userRepository.existsByEmail(email)
                : userRepository.existsByEmailAndIdNot(email, excludeUserId);
        if (taken) {
            throw new BusinessException(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS);
        }
    }

    public void validatePasswordConfirmed(String password, String confirmation) {
        if (!password.equals(confirmation)) {
            throw new BusinessException(ErrorCode.AUTH_PASSWORD_CONFIRM_MISMATCH);
        }
    }

    /** Tài khoản nhân viên chỉ nhận bốn vai trò nhân viên; khách hàng tự đăng ký nên Quản lý không tạo hay chuyển sang. */
    public void validateStaffRole(Role role) {
        if (role == null || !Role.STAFF_ROLES.contains(role)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Vai trò không hợp lệ cho nhân viên.");
        }
    }

    /** Quản lý không được tự khóa, xóa hay đổi vai trò của chính mình. */
    public void validateNotSelf(UUID currentUserId, UUID targetId) {
        if (currentUserId.equals(targetId)) {
            throw new BusinessException(ErrorCode.USER_CANNOT_MODIFY_SELF);
        }
    }

    /**
     * Hỏi mọi module có dữ liệu gắn với tài khoản (đặt bàn, đơn hàng, hóa đơn) xem có chặn xóa không.
     *
     * @throws BusinessException {@code USER_HAS_ACTIVITY} nếu có guard báo còn dữ liệu
     */
    public void validateDeletable(UUID userId) {
        if (deletionGuards.orderedStream().anyMatch(guard -> guard.hasBlockingData(userId))) {
            throw new BusinessException(ErrorCode.USER_HAS_ACTIVITY);
        }
    }
}
