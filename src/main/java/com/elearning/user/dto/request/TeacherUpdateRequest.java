package com.elearning.user.dto.request;

import com.elearning.user.enums.Gender;
import com.elearning.user.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Quản trị viên sửa giảng viên (UC008). Giống {@link TeacherCreateRequest} nhưng mật khẩu tùy chọn.
 * Đổi {@code status} là Khóa/Mở khóa giảng viên.
 *
 * @param fullName    họ tên, tối đa 255 ký tự
 * @param email       email
 * @param password    mật khẩu mới; bỏ trống thì giữ mật khẩu cũ
 * @param status      {@code ACTIVE} hoặc {@code LOCKED}
 * @param dateOfBirth ngày sinh, {@code yyyy-MM-dd}
 * @param phone       số điện thoại, chỉ chữ số
 * @param gender      giới tính
 */
public record TeacherUpdateRequest(
        @NotBlank(message = "Họ tên không được để trống") @Size(max = 255, message = "Họ tên tối đa 255 ký tự") String fullName,
        @NotBlank(message = "Email không được để trống") @Email(message = "Email không đúng định dạng") String email,
        @Size(min = 6, message = "Mật khẩu tối thiểu 6 ký tự") String password,
        @NotNull(message = "Trạng thái không được để trống") UserStatus status,
        @Past(message = "Ngày sinh phải ở quá khứ") LocalDate dateOfBirth,
        @Pattern(regexp = "\\d*", message = "Số điện thoại chỉ gồm chữ số") @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự") String phone,
        Gender gender
) {
}
