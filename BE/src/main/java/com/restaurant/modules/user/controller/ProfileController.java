package com.restaurant.modules.user.controller;

import com.restaurant.common.response.ApiResponse;
import com.restaurant.common.security.SecurityContextUtil;
import com.restaurant.modules.user.dto.request.PasswordChangeRequest;
import com.restaurant.modules.user.dto.request.ProfileUpdateRequest;
import com.restaurant.modules.user.dto.response.ProfileResponse;
import com.restaurant.modules.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** Hồ sơ và mật khẩu của chính người đăng nhập, mọi vai trò. */
@RestController
@RequestMapping("/users/me")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @GetMapping
    public ApiResponse<ProfileResponse> getMe() {
        return ApiResponse.of(userService.getProfile(SecurityContextUtil.currentUserId()));
    }

    @PutMapping
    public ApiResponse<ProfileResponse> updateMe(@Valid @RequestBody ProfileUpdateRequest request) {
        return ApiResponse.of(userService.updateProfile(SecurityContextUtil.currentUserId(), request));
    }

    @PutMapping(path = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ProfileResponse> updateAvatar(@RequestPart("file") MultipartFile file) {
        return ApiResponse.of(userService.updateAvatar(SecurityContextUtil.currentUserId(), file));
    }

    @PutMapping("/password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        userService.changePassword(SecurityContextUtil.currentUserId(), request);
        return ApiResponse.of(null);
    }
}
