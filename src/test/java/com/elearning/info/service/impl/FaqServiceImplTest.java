package com.elearning.info.service.impl;

import com.elearning.common.exception.BusinessException;
import com.elearning.info.dto.request.FaqCreateRequest;
import com.elearning.info.dto.request.FaqUpdateRequest;
import com.elearning.info.dto.response.FaqResponse;
import com.elearning.info.entity.Faq;
import com.elearning.info.mapper.FaqMapper;
import com.elearning.info.repository.FaqRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FaqServiceImplTest {

    @Mock
    private FaqRepository faqRepository;

    private FaqServiceImpl service;

    private final UUID adminId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new FaqServiceImpl(faqRepository, Mappers.getMapper(FaqMapper.class));
    }

    @Test
    void createFaq_recordsCreatorAndFlushes() {
        when(faqRepository.saveAndFlush(any(Faq.class))).thenAnswer(inv -> inv.getArgument(0));

        FaqResponse response = service.createFaq(adminId, new FaqCreateRequest("Câu hỏi?", "Trả lời."));

        ArgumentCaptor<Faq> saved = ArgumentCaptor.forClass(Faq.class);
        verify(faqRepository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getCreatedBy()).isEqualTo(adminId);
        assertThat(saved.getValue().getId()).isNotNull();
        assertThat(response.question()).isEqualTo("Câu hỏi?");
        assertThat(response.answer()).isEqualTo("Trả lời.");
    }

    @Test
    void updateFaq_replacesQuestionAndAnswerAndFlushes() {
        Faq faq = faq();
        when(faqRepository.findById(faq.getId())).thenReturn(Optional.of(faq));

        FaqResponse response = service.updateFaq(faq.getId(), new FaqUpdateRequest("Mới?", "Đáp mới."));

        assertThat(faq.getQuestion()).isEqualTo("Mới?");
        assertThat(faq.getAnswer()).isEqualTo("Đáp mới.");
        assertThat(faq.getCreatedBy()).isEqualTo(adminId);
        assertThat(response.question()).isEqualTo("Mới?");
        verify(faqRepository).flush();
    }

    @Test
    void getUpdateAndDelete_unknownIdIsNotFound() {
        UUID id = UUID.randomUUID();
        when(faqRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getFaq(id)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.updateFaq(id, new FaqUpdateRequest("Q", "A")))
                .isInstanceOfSatisfying(BusinessException.class, e -> assertThat(e.getCode()).isEqualTo("NOT_FOUND"));
        assertThatThrownBy(() -> service.deleteFaq(id)).isInstanceOf(BusinessException.class);
        verify(faqRepository, never()).flush();
    }

    @Test
    void deleteFaq_softDeletes() {
        Faq faq = faq();
        when(faqRepository.findById(faq.getId())).thenReturn(Optional.of(faq));

        service.deleteFaq(faq.getId());

        assertThat(faq.getDeletedAt()).isNotNull();
        verify(faqRepository, never()).delete(any(Faq.class));
    }

    private Faq faq() {
        return Faq.builder().id(UUID.randomUUID()).question("Cũ?").answer("Cũ.").createdBy(adminId).build();
    }
}
