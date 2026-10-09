package com.elearning.info.controller;

import com.elearning.common.response.ApiResponse;
import com.elearning.common.response.PageRequestParams;
import com.elearning.common.response.PageResponse;
import com.elearning.common.security.SecurityContextUtil;
import com.elearning.info.dto.request.FaqCreateRequest;
import com.elearning.info.dto.request.FaqUpdateRequest;
import com.elearning.info.dto.response.FaqResponse;
import com.elearning.info.service.FaqService;
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
 * Quản trị viên quản lý câu hỏi thường gặp (UC013). Theo SRS chỉ Quản trị viên xem và quản lý.
 */
@RestController
@RequestMapping("/admin/faqs")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class FaqController {

    private final FaqService faqService;

    @GetMapping
    public ApiResponse<PageResponse<FaqResponse>> searchFaqs(
            @RequestParam(required = false) String question, PageRequestParams params) {
        return ApiResponse.of(PageResponse.of(faqService.searchFaqs(question, params)));
    }

    @GetMapping("/{id}")
    public ApiResponse<FaqResponse> getFaq(@PathVariable UUID id) {
        return ApiResponse.of(faqService.getFaq(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<FaqResponse> createFaq(@Valid @RequestBody FaqCreateRequest request) {
        return ApiResponse.of(faqService.createFaq(SecurityContextUtil.currentUserId(), request));
    }

    @PutMapping("/{id}")
    public ApiResponse<FaqResponse> updateFaq(@PathVariable UUID id, @Valid @RequestBody FaqUpdateRequest request) {
        return ApiResponse.of(faqService.updateFaq(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteFaq(@PathVariable UUID id) {
        faqService.deleteFaq(id);
        return ApiResponse.of(null);
    }
}
