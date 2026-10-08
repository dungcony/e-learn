package com.elearning.user.validator;

import com.elearning.common.exception.BusinessException;
import com.elearning.common.exception.ErrorCode;
import com.elearning.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

/**
 * Kiểm tra nghiệp vụ của tài khoản cần so khớp nhiều trường hoặc truy vấn DB (rule 2.6 tầng 2).
 */
@Component
@RequiredArgsConstructor
public class UserValidator {

    private final UserRepository userRepository;

    /**
     * @throws BusinessException {@code AUTH_PASSWORD_CONFIRM_MISMATCH} nếu hai mật khẩu khác nhau (kể cả một bên null)
     */
    public void validatePasswordConfirm(String password, String confirmPassword) {
        if (!Objects.equals(password, confirmPassword)) {
            throw new BusinessException(ErrorCode.AUTH_PASSWORD_CONFIRM_MISMATCH);
        }
    }

    /**
     * @param email         email đã chuẩn hóa chữ thường
     * @param excludeUserId tài khoản được bỏ qua khi đổi email của chính nó; null khi tạo mới
     * @throws BusinessException {@code AUTH_EMAIL_ALREADY_EXISTS} nếu tài khoản khác chưa xóa đang dùng email này
     */
    public void validateEmailNotTaken(String email, UUID excludeUserId) {
        boolean taken = excludeUserId == null
                ? userRepository.existsByEmail(email)
                : userRepository.existsByEmailAndIdNot(email, excludeUserId);
        if (taken) {
            throw new BusinessException(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS);
        }
    }
}
