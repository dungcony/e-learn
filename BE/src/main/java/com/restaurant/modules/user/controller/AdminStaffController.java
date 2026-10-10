package com.restaurant.modules.user.controller;

import com.restaurant.common.response.ApiResponse;
import com.restaurant.common.response.PageRequestParams;
import com.restaurant.common.response.PageResponse;
import com.restaurant.common.security.SecurityContextUtil;
import com.restaurant.modules.user.dto.request.StaffCreateRequest;
import com.restaurant.modules.user.dto.request.StaffUpdateRequest;
import com.restaurant.modules.user.dto.request.UserSearchRequest;
import com.restaurant.modules.user.dto.request.UserStatusUpdateRequest;
import com.restaurant.modules.user.dto.response.UserDetailResponse;
import com.restaurant.modules.user.dto.response.UserSummaryResponse;
import com.restaurant.modules.user.enums.UserGroup;
import com.restaurant.modules.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/** Quản lý nhân viên (UC006, UC008); chỉ Quản lý. */
@RestController
@RequestMapping("/admin/staff")
@PreAuthorize("hasRole('MANAGER')")
@RequiredArgsConstructor
public class AdminStaffController {

    private final UserService userService;

    @GetMapping
    public ApiResponse<PageResponse<UserSummaryResponse>> searchStaff(UserSearchRequest request, PageRequestParams params) {
        return ApiResponse.of(PageResponse.of(userService.searchUsers(UserGroup.STAFF, request, params)));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserDetailResponse> getStaff(@PathVariable UUID id) {
        return ApiResponse.of(userService.getUser(UserGroup.STAFF, id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserDetailResponse> createStaff(@Valid @RequestBody StaffCreateRequest request) {
        return ApiResponse.of(userService.createStaff(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<UserDetailResponse> updateStaff(@PathVariable UUID id, @Valid @RequestBody StaffUpdateRequest request) {
        return ApiResponse.of(userService.updateStaff(SecurityContextUtil.currentUserId(), id, request));
    }

    @PutMapping(path = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<UserDetailResponse> updateStaffAvatar(@PathVariable UUID id, @RequestPart("file") MultipartFile file) {
        return ApiResponse.of(userService.updateStaffAvatar(id, file));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<UserDetailResponse> changeStaffStatus(@PathVariable UUID id,
                                                             @Valid @RequestBody UserStatusUpdateRequest request) {
        return ApiResponse.of(userService.changeStatus(UserGroup.STAFF, SecurityContextUtil.currentUserId(), id, request.status()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteStaff(@PathVariable UUID id) {
        userService.deleteUser(UserGroup.STAFF, SecurityContextUtil.currentUserId(), id);
        return ApiResponse.of(null);
    }
}
