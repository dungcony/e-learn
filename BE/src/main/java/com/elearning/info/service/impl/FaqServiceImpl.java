package com.elearning.info.service.impl;

import com.elearning.common.exception.BusinessException;
import com.elearning.common.exception.ErrorCode;
import com.elearning.common.response.PageRequestParams;
import com.elearning.common.util.SpecificationUtils;
import com.elearning.info.dto.request.FaqCreateRequest;
import com.elearning.info.dto.request.FaqUpdateRequest;
import com.elearning.info.dto.response.FaqResponse;
import com.elearning.info.entity.Faq;
import com.elearning.info.mapper.FaqMapper;
import com.elearning.info.repository.FaqRepository;
import com.elearning.info.service.FaqService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FaqServiceImpl implements FaqService {

    private static final Map<String, String> SORTABLE_FIELDS = Map.of("created_at", "createdAt", "question", "question");

    private final FaqRepository faqRepository;
    private final FaqMapper faqMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<FaqResponse> searchFaqs(String question, PageRequestParams params) {
        return faqRepository
                .findAll(SpecificationUtils.<Faq>containsIgnoreCase("question", question), params.toPageable(SORTABLE_FIELDS, "createdAt"))
                .map(faqMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public FaqResponse getFaq(UUID id) {
        return faqMapper.toResponse(find(id));
    }

    @Override
    @Transactional
    public FaqResponse createFaq(UUID adminId, FaqCreateRequest request) {
        Faq faq = faqMapper.toFaq(request);
        faq.setId(UUID.randomUUID());
        faq.setCreatedBy(adminId);
        // Flush để created_at, updated_at (gán lúc flush) có giá trị trong response.
        return faqMapper.toResponse(faqRepository.saveAndFlush(faq));
    }

    @Override
    @Transactional
    public FaqResponse updateFaq(UUID id, FaqUpdateRequest request) {
        Faq faq = find(id);
        faqMapper.updateFaq(request, faq);
        faqRepository.flush();
        return faqMapper.toResponse(faq);
    }

    @Override
    @Transactional
    public void deleteFaq(UUID id) {
        find(id).setDeletedAt(Instant.now());
    }

    private Faq find(UUID id) {
        return faqRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy câu hỏi thường gặp."));
    }
}
