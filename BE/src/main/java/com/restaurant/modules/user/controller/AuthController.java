package com.restaurant.modules.user.controller;

import com.restaurant.common.response.ApiResponse;
import com.restaurant.modules.user.dto.request.EmailResendRequest;
import com.restaurant.modules.user.dto.request.EmailVerifyRequest;
import com.restaurant.modules.user.dto.request.PasswordForgotRequest;
import com.restaurant.modules.user.dto.request.PasswordResetRequest;
import com.restaurant.modules.user.dto.request.UserLoginRequest;
import com.restaurant.modules.user.dto.request.UserRegisterRequest;
import com.restaurant.modules.user.dto.response.AuthResponse;
import com.restaurant.modules.user.dto.response.ProfileResponse;
import com.restaurant.modules.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Các endpoint công khai của Khách: đăng ký, xác thực email, đăng nhập, quên và đặt lại mật khẩu. */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProfileResponse> register(@Valid @RequestBody UserRegisterRequest request) {
        return ApiResponse.of(authService.register(request));
    }

    @PostMapping("/verify-email")
    public ApiResponse<Void> verifyEmail(@Valid @RequestBody EmailVerifyRequest request) {
        authService.verifyEmail(request.token());
        return ApiResponse.of(null);
    }

    @PostMapping("/resend-verification")
    public ApiResponse<Void> resendVerification(@Valid @RequestBody EmailResendRequest request) {
        authService.resendVerification(request);
        return ApiResponse.of(null);
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody UserLoginRequest request) {
        return ApiResponse.of(authService.login(request));
    }

    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody PasswordForgotRequest request) {
        authService.forgotPassword(request);
        return ApiResponse.of(null);
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        authService.resetPassword(request);
        return ApiResponse.of(null);
    }
}
