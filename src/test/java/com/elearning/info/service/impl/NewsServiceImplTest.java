package com.elearning.info.service.impl;

import com.elearning.common.exception.BusinessException;
import com.elearning.common.response.PageRequestParams;
import com.elearning.info.dto.request.NewsCreateRequest;
import com.elearning.info.dto.request.NewsUpdateRequest;
import com.elearning.info.dto.response.NewsDetailResponse;
import com.elearning.info.entity.News;
import com.elearning.info.mapper.NewsMapper;
import com.elearning.info.repository.NewsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewsServiceImplTest {

    @Mock
    private NewsRepository newsRepository;

    private NewsServiceImpl service;

    private final UUID adminId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new NewsServiceImpl(newsRepository, Mappers.getMapper(NewsMapper.class));
    }

    @Test
    void createNews_recordsAuthorAndFlushesSoTimestampsAreInTheResponse() {
        when(newsRepository.saveAndFlush(any(News.class))).thenAnswer(inv -> inv.getArgument(0));

        NewsDetailResponse response = service.createNews(adminId, new NewsCreateRequest("Linear Algebra", "Nội dung"));

        ArgumentCaptor<News> saved = ArgumentCaptor.forClass(News.class);
        verify(newsRepository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getAuthorId()).isEqualTo(adminId);
        assertThat(saved.getValue().getId()).isNotNull();
        assertThat(response.title()).isEqualTo("Linear Algebra");
        assertThat(response.authorId()).isEqualTo(adminId);
    }

    @Test
    void updateNews_replacesTitleAndContentKeepsAuthorAndFlushes() {
        News news = news();
        when(newsRepository.findById(news.getId())).thenReturn(Optional.of(news));

        service.updateNews(news.getId(), new NewsUpdateRequest("Tiêu đề mới", "Nội dung mới"));

        assertThat(news.getTitle()).isEqualTo("Tiêu đề mới");
        assertThat(news.getContent()).isEqualTo("Nội dung mới");
        assertThat(news.getAuthorId()).isEqualTo(adminId);
        InOrder order = inOrder(newsRepository);
        order.verify(newsRepository).findById(news.getId());
        order.verify(newsRepository).flush();
    }

    @Test
    void getUpdateAndDelete_unknownIdIsNotFound() {
        UUID id = UUID.randomUUID();
        when(newsRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getNews(id)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.updateNews(id, new NewsUpdateRequest("T", "C")))
                .isInstanceOfSatisfying(BusinessException.class, e -> assertThat(e.getCode()).isEqualTo("NOT_FOUND"));
        assertThatThrownBy(() -> service.deleteNews(id)).isInstanceOf(BusinessException.class);
        verify(newsRepository, never()).flush();
    }

    @Test
    void deleteNews_softDeletes() {
        News news = news();
        when(newsRepository.findById(news.getId())).thenReturn(Optional.of(news));

        service.deleteNews(news.getId());

        assertThat(news.getDeletedAt()).isNotNull();
        verify(newsRepository, never()).delete(any(News.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchNews_sortsByRequestedFieldAndFallsBackToNewestFirst() {
        when(newsRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        service.searchNews("linear", PageRequestParams.of(1, 20, "title", "asc"));
        service.searchNews(null, PageRequestParams.of(1, 20, "author_id", "asc"));

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(newsRepository, org.mockito.Mockito.times(2)).findAll(any(Specification.class), pageable.capture());
        assertThat(pageable.getAllValues().get(0).getSort().getOrderFor("title").getDirection()).isEqualTo(Sort.Direction.ASC);
        assertThat(pageable.getAllValues().get(1).getSort().getOrderFor("createdAt")).isNotNull();
        assertThat(pageable.getAllValues().get(1).getSort().getOrderFor("authorId")).isNull();
    }

    private News news() {
        return News.builder().id(UUID.randomUUID()).title("Cũ").content("Cũ").authorId(adminId).build();
    }
}
