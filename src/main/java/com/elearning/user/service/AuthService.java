package com.elearning.user.service;

import com.elearning.user.dto.request.PasswordForgotRequest;
import com.elearning.user.dto.request.PasswordResetRequest;
import com.elearning.user.dto.request.UserLoginRequest;
import com.elearning.user.dto.request.UserRegisterRequest;
import com.elearning.user.dto.response.AuthResponse;
import com.elearning.user.dto.response.ProfileResponse;

/**
 * Đăng ký, đăng nhập và đặt lại mật khẩu (UC001, UC003, UC004) của người chưa đăng nhập.
 */
public interface AuthService {

    /**
     * Tạo tài khoản học viên trạng thái {@code ACTIVE}; không tự đăng nhập.
     *
     * @param request thông tin đăng ký
     * @return thông tin tài khoản vừa tạo
     * @throws com.elearning.common.exception.BusinessException {@code AUTH_PASSWORD_CONFIRM_MISMATCH} nếu mật khẩu
     *                                                          xác nhận không trùng; {@code AUTH_EMAIL_ALREADY_EXISTS}
     *                                                          nếu email đã có tài khoản chưa xóa
     */
    ProfileResponse registerStudent(UserRegisterRequest request);

    /**
     * Xác thực email và mật khẩu rồi cấp access token.
     *
     * @param request email và mật khẩu
     * @return token kèm vai trò
     * @throws com.elearning.common.exception.BusinessException {@code AUTH_CREDENTIALS_INVALID} nếu email không có
     *                                                          tài khoản hoặc sai mật khẩu (dùng chung một mã để không
     *                                                          lộ email nào đã đăng ký); {@code AUTH_ACCOUNT_BLOCKED}
     *                                                          nếu tài khoản đang bị khóa
     */
    AuthResponse login(UserLoginRequest request);

    /**
     * Tạo token đặt lại mật khẩu hạn 60 phút và gửi link chứa token qua email. Token cũ chưa dùng của người này bị
     * vô hiệu.
     * <p>
     * <b>Tác dụng phụ:</b> gửi email sau khi transaction commit; lỗi gửi chỉ được log, không làm request thất bại.
     *
     * @param request email của tài khoản quên mật khẩu
     * @throws com.elearning.common.exception.BusinessException {@code NOT_FOUND} nếu không có tài khoản với email này
     */
    void requestPasswordReset(PasswordForgotRequest request);

    /**
     * Đặt mật khẩu mới bằng token trong link. Token chỉ dùng được một lần.
     * <p>
     * <b>Locking:</b> khóa bi quan dòng token ({@code FOR UPDATE}) để hai request cùng token không cùng thành công.
     *
     * @param request token và mật khẩu mới
     * @throws com.elearning.common.exception.BusinessException {@code AUTH_PASSWORD_CONFIRM_MISMATCH} nếu mật khẩu
     *                                                          xác nhận không trùng; {@code AUTH_CODE_INVALID} nếu token
     *                                                          sai, quá hạn, đã dùng hoặc tài khoản đã bị xóa
     */
    void resetPassword(PasswordResetRequest request);
}
