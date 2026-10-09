package com.elearning.user.controller;

import com.elearning.common.response.ApiResponse;
import com.elearning.common.response.PageRequestParams;
import com.elearning.common.response.PageResponse;
import com.elearning.user.dto.request.UserSearchRequest;
import com.elearning.user.dto.request.UserStatusUpdateRequest;
import com.elearning.user.dto.response.UserDetailResponse;
import com.elearning.user.dto.response.UserSummaryResponse;
import com.elearning.user.enums.Role;
import com.elearning.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Quản trị viên quản lý học viên: tìm, xem, khóa/mở khóa, xóa (UC006, UC010). Không thêm hoặc sửa học viên.
 */
@RestController
@RequestMapping("/admin/students")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminStudentController {

    private final UserService userService;

    @GetMapping
    public ApiResponse<PageResponse<UserSummaryResponse>> searchStudents(
            @ModelAttribute UserSearchRequest search, PageRequestParams params) {
        return ApiResponse.of(PageResponse.of(userService.searchUsers(Role.STUDENT, search, params)));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserDetailResponse> getStudent(@PathVariable UUID id) {
        return ApiResponse.of(userService.getUser(id, Role.STUDENT));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<UserDetailResponse> changeStudentStatus(
            @PathVariable UUID id, @Valid @RequestBody UserStatusUpdateRequest request) {
        return ApiResponse.of(userService.changeStudentStatus(id, request.status()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteStudent(@PathVariable UUID id) {
        userService.deleteUser(id, Role.STUDENT);
        return ApiResponse.of(null);
    }
}
