package com.elearning.user.controller;

import com.elearning.common.response.ApiResponse;
import com.elearning.user.dto.request.PasswordForgotRequest;
import com.elearning.user.dto.request.PasswordResetRequest;
import com.elearning.user.dto.request.UserLoginRequest;
import com.elearning.user.dto.request.UserRegisterRequest;
import com.elearning.user.dto.response.AuthResponse;
import com.elearning.user.dto.response.ProfileResponse;
import com.elearning.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Các endpoint công khai của Khách: đăng ký, đăng nhập, đặt lại mật khẩu (UC001, UC003, UC004).
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProfileResponse> register(@Valid @RequestBody UserRegisterRequest request) {
        return ApiResponse.of(authService.registerStudent(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody UserLoginRequest request) {
        return ApiResponse.of(authService.login(request));
    }

    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody PasswordForgotRequest request) {
        authService.requestPasswordReset(request);
        return ApiResponse.of(null);
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        authService.resetPassword(request);
        return ApiResponse.of(null);
    }
}
