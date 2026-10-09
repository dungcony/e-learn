package com.elearning.user.dto.request;

import com.elearning.user.enums.Gender;

/**
 * Tiêu chí tìm kiếm giảng viên, học viên (UC006, Bảng 2-10); tiêu chí để trống thì không lọc.
 *
 * @param name   tên chứa chuỗi này, không phân biệt hoa thường
 * @param email  email chứa chuỗi này
 * @param phone  số điện thoại chứa chuỗi này
 * @param gender giới tính
 */
public record UserSearchRequest(String name, String email, String phone, Gender gender) {
}
