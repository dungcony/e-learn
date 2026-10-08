package com.elearning.user.enums;

/**
 * Vai trò của tài khoản. Tên enum là hậu tố sau {@code ROLE_} trong authorities của JWT và trong
 * {@code @PreAuthorize("hasRole('...')")}.
 */
public enum Role {
    /** Quản trị viên (QTV). */
    ADMIN,
    /** Giảng viên (GV). */
    TEACHER,
    /** Học viên (HV). */
    STUDENT
}
