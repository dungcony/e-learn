package com.elearning.user.dto.request;

import com.elearning.user.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Cập nhật thông tin cá nhân (UC005, Bảng 2-8). Gửi đủ form, trường tùy chọn bỏ trống sẽ bị xóa giá trị cũ.
 *
 * @param fullName        họ tên, tối đa 255 ký tự
 * @param email           email, bắt buộc
 * @param dateOfBirth     ngày sinh, {@code yyyy-MM-dd}, phải ở quá khứ
 * @param phone           số điện thoại, chỉ chữ số
 * @param gender          giới tính
 * @param password        mật khẩu mới; chỉ Giảng viên, Quản trị viên được gửi (ghi chú UC002), bỏ trống thì giữ mật khẩu cũ
 * @param confirmPassword nhập lại mật khẩu mới, đi kèm {@code password}
 */
public record ProfileUpdateRequest(
        @Size(max = 255, message = "Họ tên tối đa 255 ký tự") String fullName,
        @NotBlank(message = "Email không được để trống") @Email(message = "Email không đúng định dạng") String email,
        @Past(message = "Ngày sinh phải ở quá khứ") LocalDate dateOfBirth,
        @Pattern(regexp = "\\d*", message = "Số điện thoại chỉ gồm chữ số") @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự") String phone,
        Gender gender,
        @Size(min = 6, message = "Mật khẩu tối thiểu 6 ký tự") String password,
        String confirmPassword
) {
}
