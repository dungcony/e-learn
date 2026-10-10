package com.restaurant.modules.user.controller;

import com.restaurant.common.response.ApiResponse;
import com.restaurant.common.response.PageRequestParams;
import com.restaurant.common.response.PageResponse;
import com.restaurant.common.security.SecurityContextUtil;
import com.restaurant.modules.user.dto.request.UserSearchRequest;
import com.restaurant.modules.user.dto.request.UserStatusUpdateRequest;
import com.restaurant.modules.user.dto.response.UserDetailResponse;
import com.restaurant.modules.user.dto.response.UserSummaryResponse;
import com.restaurant.modules.user.enums.UserGroup;
import com.restaurant.modules.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Quản lý khách hàng (UC006, UC009): xem, khóa/mở khóa, xóa; không thêm hay sửa vì khách tự đăng ký. Chỉ Quản lý. */
@RestController
@RequestMapping("/admin/customers")
@PreAuthorize("hasRole('MANAGER')")
@RequiredArgsConstructor
public class AdminCustomerController {

    private final UserService userService;

    @GetMapping
    public ApiResponse<PageResponse<UserSummaryResponse>> searchCustomers(UserSearchRequest request, PageRequestParams params) {
        return ApiResponse.of(PageResponse.of(userService.searchUsers(UserGroup.CUSTOMER, request, params)));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserDetailResponse> getCustomer(@PathVariable UUID id) {
        return ApiResponse.of(userService.getUser(UserGroup.CUSTOMER, id));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<UserDetailResponse> changeCustomerStatus(@PathVariable UUID id,
                                                                @Valid @RequestBody UserStatusUpdateRequest request) {
        return ApiResponse.of(userService.changeStatus(UserGroup.CUSTOMER, SecurityContextUtil.currentUserId(), id, request.status()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCustomer(@PathVariable UUID id) {
        userService.deleteUser(UserGroup.CUSTOMER, SecurityContextUtil.currentUserId(), id);
        return ApiResponse.of(null);
    }
}
