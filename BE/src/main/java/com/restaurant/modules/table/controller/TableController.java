package com.restaurant.modules.table.controller;

import com.restaurant.common.response.ApiResponse;
import com.restaurant.common.response.PageRequestParams;
import com.restaurant.common.response.PageResponse;
import com.restaurant.modules.table.dto.request.TableCreateRequest;
import com.restaurant.modules.table.dto.request.TableSearchRequest;
import com.restaurant.modules.table.dto.request.TableUpdateRequest;
import com.restaurant.modules.table.dto.response.TableResponse;
import com.restaurant.modules.table.service.TableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Sơ đồ bàn và quản lý bàn (UC007, UC011). Nhân viên phục vụ, Thu ngân và Quản lý xem được; chỉ Quản lý thêm, sửa, xóa.
 * Sơ đồ bàn là {@code GET /tables} với {@code page_size} đủ lớn, client tự gom nhóm theo khu vực.
 */
@RestController
@RequestMapping("/tables")
@RequiredArgsConstructor
public class TableController {

    private final TableService tableService;

    @GetMapping
    @PreAuthorize("hasAnyRole('WAITER', 'CASHIER', 'MANAGER')")
    public ApiResponse<PageResponse<TableResponse>> searchTables(@Valid TableSearchRequest request, PageRequestParams params) {
        return ApiResponse.of(PageResponse.of(tableService.searchTables(request, params)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('WAITER', 'CASHIER', 'MANAGER')")
    public ApiResponse<TableResponse> getTable(@PathVariable UUID id) {
        return ApiResponse.of(tableService.getTable(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TableResponse> createTable(@Valid @RequestBody TableCreateRequest request) {
        return ApiResponse.of(tableService.createTable(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ApiResponse<TableResponse> updateTable(@PathVariable UUID id, @Valid @RequestBody TableUpdateRequest request) {
        return ApiResponse.of(tableService.updateTable(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ApiResponse<Void> deleteTable(@PathVariable UUID id) {
        tableService.deleteTable(id);
        return ApiResponse.of(null);
    }
}
