package com.elearning.user.controller;

import com.elearning.common.response.ApiResponse;
import com.elearning.common.security.SecurityContextUtil;
import com.elearning.user.dto.request.PasswordChangeRequest;
import com.elearning.user.dto.request.ProfileUpdateRequest;
import com.elearning.user.dto.response.ProfileResponse;
import com.elearning.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Thông tin cá nhân của người đã đăng nhập (UC002, UC005).
 */
@RestController
@RequestMapping("/users/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ApiResponse<ProfileResponse> getMyProfile() {
        return ApiResponse.of(userService.getProfile(SecurityContextUtil.currentUserId()));
    }

    @PutMapping
    public ApiResponse<ProfileResponse> updateMyProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        return ApiResponse.of(userService.updateProfile(SecurityContextUtil.currentUserId(), request));
    }

    @PutMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ProfileResponse> updateMyAvatar(@RequestPart("file") MultipartFile file) {
        return ApiResponse.of(userService.updateAvatar(SecurityContextUtil.currentUserId(), file));
    }

    @PutMapping("/password")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<Void> changeMyPassword(@Valid @RequestBody PasswordChangeRequest request) {
        userService.changePassword(SecurityContextUtil.currentUserId(), request);
        return ApiResponse.of(null);
    }
}
