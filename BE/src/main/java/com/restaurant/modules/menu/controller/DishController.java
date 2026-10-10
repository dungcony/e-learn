package com.restaurant.modules.menu.controller;

import com.restaurant.common.response.ApiResponse;
import com.restaurant.common.response.PageRequestParams;
import com.restaurant.common.response.PageResponse;
import com.restaurant.modules.menu.dto.request.DishCreateRequest;
import com.restaurant.modules.menu.dto.request.DishSearchRequest;
import com.restaurant.modules.menu.dto.request.DishUpdateRequest;
import com.restaurant.modules.menu.dto.response.DishDetailResponse;
import com.restaurant.modules.menu.dto.response.DishSummaryResponse;
import com.restaurant.modules.menu.service.DishService;
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

/** Quản lý món ăn (UC007, UC010); chỉ Quản lý. */
@RestController
@RequestMapping("/dishes")
@PreAuthorize("hasRole('MANAGER')")
@RequiredArgsConstructor
public class DishController {

    private final DishService dishService;

    @GetMapping
    public ApiResponse<PageResponse<DishSummaryResponse>> searchDishes(@Valid DishSearchRequest request, PageRequestParams params) {
        return ApiResponse.of(PageResponse.of(dishService.searchDishes(request, params)));
    }

    @GetMapping("/{id}")
    public ApiResponse<DishDetailResponse> getDish(@PathVariable UUID id) {
        return ApiResponse.of(dishService.getDish(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<DishDetailResponse> createDish(@Valid @RequestBody DishCreateRequest request) {
        return ApiResponse.of(dishService.createDish(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<DishDetailResponse> updateDish(@PathVariable UUID id, @Valid @RequestBody DishUpdateRequest request) {
        return ApiResponse.of(dishService.updateDish(id, request));
    }

    @PutMapping(path = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<DishDetailResponse> updateDishImage(@PathVariable UUID id, @RequestPart("file") MultipartFile file) {
        return ApiResponse.of(dishService.updateDishImage(id, file));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDish(@PathVariable UUID id) {
        dishService.deleteDish(id);
        return ApiResponse.of(null);
    }
}
