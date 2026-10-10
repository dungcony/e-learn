package com.restaurant.modules.user.dto.request;

/** Quy tắc mật khẩu dùng chung cho mọi request: ít nhất 8 ký tự gồm chữ và số; tối đa 72 vì BCrypt chỉ đọc 72 byte đầu. */
public final class PasswordRules {

    public static final int MIN_LENGTH = 8;
    public static final int MAX_LENGTH = 72;
    public static final String PATTERN = "^(?=.*[A-Za-z])(?=.*\\d).+$";
    public static final String LENGTH_MESSAGE = "Mật khẩu phải có từ 8 đến 72 ký tự";
    public static final String PATTERN_MESSAGE = "Mật khẩu phải gồm cả chữ và số";

    private PasswordRules() {
    }
}
