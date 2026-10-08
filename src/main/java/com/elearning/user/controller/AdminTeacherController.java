package com.elearning.user.controller;

import com.elearning.common.response.ApiResponse;
import com.elearning.common.response.PageRequestParams;
import com.elearning.common.response.PageResponse;
import com.elearning.user.dto.request.TeacherCreateRequest;
import com.elearning.user.dto.request.TeacherUpdateRequest;
import com.elearning.user.dto.request.UserSearchRequest;
import com.elearning.user.dto.response.UserDetailResponse;
import com.elearning.user.dto.response.UserSummaryResponse;
import com.elearning.user.enums.Role;
import com.elearning.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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

/**
 * Quản trị viên quản lý giảng viên, gồm tìm kiếm (UC006, UC008).
 */
@RestController
@RequestMapping("/admin/teachers")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminTeacherController {

    private final UserService userService;

    @GetMapping
    public ApiResponse<PageResponse<UserSummaryResponse>> searchTeachers(
            @ModelAttribute UserSearchRequest search, PageRequestParams params) {
        return ApiResponse.of(PageResponse.of(userService.searchUsers(Role.TEACHER, search, params)));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserDetailResponse> getTeacher(@PathVariable UUID id) {
        return ApiResponse.of(userService.getUser(id, Role.TEACHER));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserDetailResponse> createTeacher(@Valid @RequestBody TeacherCreateRequest request) {
        return ApiResponse.of(userService.createTeacher(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<UserDetailResponse> updateTeacher(
            @PathVariable UUID id, @Valid @RequestBody TeacherUpdateRequest request) {
        return ApiResponse.of(userService.updateTeacher(id, request));
    }

    @PutMapping(value = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<UserDetailResponse> updateTeacherAvatar(
            @PathVariable UUID id, @RequestPart("file") MultipartFile file) {
        return ApiResponse.of(userService.updateTeacherAvatar(id, file));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTeacher(@PathVariable UUID id) {
        userService.deleteUser(id, Role.TEACHER);
        return ApiResponse.of(null);
    }
}
