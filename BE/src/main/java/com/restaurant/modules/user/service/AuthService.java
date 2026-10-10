package com.restaurant.modules.user.service;

import com.restaurant.modules.user.dto.request.EmailResendRequest;
import com.restaurant.modules.user.dto.request.PasswordForgotRequest;
import com.restaurant.modules.user.dto.request.PasswordResetRequest;
import com.restaurant.modules.user.dto.request.UserLoginRequest;
import com.restaurant.modules.user.dto.request.UserRegisterRequest;
import com.restaurant.modules.user.dto.response.AuthResponse;
import com.restaurant.modules.user.dto.response.ProfileResponse;

/**
 * Đăng ký, đăng nhập, xác thực email và đặt lại mật khẩu (UC001, UC002, UC004). Mọi liên kết gửi qua email dùng token một
 * lần, chỉ lưu băm trong CSDL.
 */
public interface AuthService {

    /**
     * Tạo tài khoản khách hàng chưa xác thực email và gửi liên kết xác thực (hiệu lực 24 giờ) sau khi commit.
     *
     * @param request thông tin đăng ký
     * @return hồ sơ vừa tạo
     * @throws com.restaurant.common.exception.BusinessException {@code AUTH_EMAIL_ALREADY_EXISTS} nếu email đã có tài khoản,
     *                                                          {@code AUTH_PASSWORD_CONFIRM_MISMATCH} nếu xác nhận mật khẩu lệch
     */
    ProfileResponse register(UserRegisterRequest request);

    /**
     * Xác thực email bằng token trong liên kết.
     *
     * @param rawToken token thô lấy từ liên kết
     * @throws com.restaurant.common.exception.BusinessException {@code AUTH_CODE_INVALID} nếu token sai, hết hạn hoặc đã dùng
     */
    void verifyEmail(String rawToken);

    /**
     * Phát lại liên kết xác thực; token cũ chưa dùng bị vô hiệu.
     *
     * @throws com.restaurant.common.exception.BusinessException {@code NOT_FOUND} nếu không có tài khoản ứng với email,
     *                                                          {@code AUTH_ACCOUNT_ALREADY_VERIFIED} nếu đã xác thực
     */
    void resendVerification(EmailResendRequest request);

    /**
     * Đăng nhập và cấp access token.
     *
     * @throws com.restaurant.common.exception.BusinessException {@code AUTH_CREDENTIALS_INVALID} nếu sai email hoặc mật khẩu,
     *                                                          {@code AUTH_ACCOUNT_BLOCKED} nếu tài khoản bị khóa,
     *                                                          {@code AUTH_ACCOUNT_NOT_VERIFIED} nếu khách hàng chưa xác thực email
     */
    AuthResponse login(UserLoginRequest request);

    /**
     * Gửi liên kết đặt lại mật khẩu (hiệu lực 60 phút); các liên kết đặt lại cũ bị vô hiệu.
     *
     * @throws com.restaurant.common.exception.BusinessException {@code NOT_FOUND} nếu không có tài khoản ứng với email
     */
    void forgotPassword(PasswordForgotRequest request);

    /**
     * Đặt mật khẩu mới bằng token trong liên kết; đồng thời hạ cờ đổi mật khẩu lần đầu và đánh dấu email đã xác thực.
     *
     * @throws com.restaurant.common.exception.BusinessException {@code AUTH_CODE_INVALID} nếu token sai, hết hạn hoặc đã dùng,
     *                                                          {@code AUTH_PASSWORD_CONFIRM_MISMATCH} nếu xác nhận lệch
     */
    void resetPassword(PasswordResetRequest request);
}
