package com.elearning.info.service.impl;

import com.elearning.common.exception.BusinessException;
import com.elearning.common.exception.ErrorCode;
import com.elearning.common.response.PageRequestParams;
import com.elearning.common.util.SpecificationUtils;
import com.elearning.info.dto.request.NewsCreateRequest;
import com.elearning.info.dto.request.NewsUpdateRequest;
import com.elearning.info.dto.response.NewsDetailResponse;
import com.elearning.info.dto.response.NewsSummaryResponse;
import com.elearning.info.entity.News;
import com.elearning.info.mapper.NewsMapper;
import com.elearning.info.repository.NewsRepository;
import com.elearning.info.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {

    private static final Map<String, String> SORTABLE_FIELDS = Map.of("created_at", "createdAt", "title", "title");

    private final NewsRepository newsRepository;
    private final NewsMapper newsMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<NewsSummaryResponse> searchNews(String title, PageRequestParams params) {
        return newsRepository
                .findAll(SpecificationUtils.<News>containsIgnoreCase("title", title), params.toPageable(SORTABLE_FIELDS, "createdAt"))
                .map(newsMapper::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public NewsDetailResponse getNews(UUID id) {
        return newsMapper.toDetailResponse(find(id));
    }

    @Override
    @Transactional
    public NewsDetailResponse createNews(UUID adminId, NewsCreateRequest request) {
        News news = newsMapper.toNews(request);
        news.setId(UUID.randomUUID());
        news.setAuthorId(adminId);
        // Flush để created_at, updated_at (gán lúc flush) có giá trị trong response.
        return newsMapper.toDetailResponse(newsRepository.saveAndFlush(news));
    }

    @Override
    @Transactional
    public NewsDetailResponse updateNews(UUID id, NewsUpdateRequest request) {
        News news = find(id);
        newsMapper.updateNews(request, news);
        newsRepository.flush();
        return newsMapper.toDetailResponse(news);
    }

    @Override
    @Transactional
    public void deleteNews(UUID id) {
        find(id).setDeletedAt(Instant.now());
    }

    private News find(UUID id) {
        return newsRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy tin tức."));
    }
}
