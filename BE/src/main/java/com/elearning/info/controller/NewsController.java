package com.elearning.info.controller;

import com.elearning.common.response.ApiResponse;
import com.elearning.common.response.PageRequestParams;
import com.elearning.common.response.PageResponse;
import com.elearning.common.security.SecurityContextUtil;
import com.elearning.info.dto.request.NewsCreateRequest;
import com.elearning.info.dto.request.NewsUpdateRequest;
import com.elearning.info.dto.response.NewsDetailResponse;
import com.elearning.info.dto.response.NewsSummaryResponse;
import com.elearning.info.service.NewsService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Quản trị viên quản lý tin tức (UC012). Theo SRS chỉ Quản trị viên xem và quản lý, không có trang tin tức công khai.
 */
@RestController
@RequestMapping("/admin/news")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    @GetMapping
    public ApiResponse<PageResponse<NewsSummaryResponse>> searchNews(
            @RequestParam(required = false) String title, PageRequestParams params) {
        return ApiResponse.of(PageResponse.of(newsService.searchNews(title, params)));
    }

    @GetMapping("/{id}")
    public ApiResponse<NewsDetailResponse> getNews(@PathVariable UUID id) {
        return ApiResponse.of(newsService.getNews(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<NewsDetailResponse> createNews(@Valid @RequestBody NewsCreateRequest request) {
        return ApiResponse.of(newsService.createNews(SecurityContextUtil.currentUserId(), request));
    }

    @PutMapping("/{id}")
    public ApiResponse<NewsDetailResponse> updateNews(@PathVariable UUID id, @Valid @RequestBody NewsUpdateRequest request) {
        return ApiResponse.of(newsService.updateNews(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteNews(@PathVariable UUID id) {
        newsService.deleteNews(id);
        return ApiResponse.of(null);
    }
}
