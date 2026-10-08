package com.elearning.learning.service.impl;

import com.elearning.common.exception.BusinessException;
import com.elearning.common.exception.ErrorCode;
import com.elearning.common.response.PageRequestParams;
import com.elearning.course.service.LectureService;
import com.elearning.learning.dto.request.CommentCreateRequest;
import com.elearning.learning.dto.request.CommentUpdateRequest;
import com.elearning.learning.dto.response.CommentResponse;
import com.elearning.learning.entity.Comment;
import com.elearning.learning.mapper.LearningMapper;
import com.elearning.learning.repository.CommentRepository;
import com.elearning.learning.validator.LearningAccessValidator;
import com.elearning.user.dto.response.UserBriefResponse;
import com.elearning.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private LearningAccessValidator accessValidator;
    @Mock
    private LectureService lectureService;
    @Mock
    private UserService userService;

    private CommentServiceImpl service;

    private final UUID studentId = UUID.randomUUID();
    private final UUID lectureId = UUID.randomUUID();
    private final UUID courseId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new CommentServiceImpl(commentRepository, accessValidator, lectureService, userService,
                Mappers.getMapper(LearningMapper.class));
    }

    private void stubAccess() {
        when(lectureService.getCourseIdOfLecture(lectureId)).thenReturn(courseId);
    }

    @Test
    void createComment_checksLearningAccessOfTheLecturesCourseFirst() {
        stubAccess();
        when(accessValidator.requireLearningAccess(studentId, courseId))
                .thenThrow(new BusinessException(ErrorCode.COURSE_NOT_STARTED));

        assertThatThrownBy(() -> service.createComment(studentId, lectureId, new CommentCreateRequest("Hi", null)))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo("COURSE_NOT_STARTED"));
        verify(commentRepository, never()).saveAndFlush(any());
    }

    @Test
    void createComment_rootCommentHasNoParentAndAuthorName() {
        stubAccess();
        when(commentRepository.saveAndFlush(any(Comment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userService.getUserBriefs(anyCollection())).thenReturn(List.of(new UserBriefResponse(studentId, "Học viên", "hv@x.y")));

        CommentResponse response = service.createComment(studentId, lectureId, new CommentCreateRequest("Bài này khó", null));

        assertThat(response.parentId()).isNull();
        assertThat(response.lectureId()).isEqualTo(lectureId);
        assertThat(response.author().fullName()).isEqualTo("Học viên");
        assertThat(response.replies()).isEmpty();
    }

    @Test
    void createComment_replyToRootOfSameLectureIsAccepted() {
        stubAccess();
        Comment root = comment(UUID.randomUUID(), lectureId, null);
        when(commentRepository.findById(root.getId())).thenReturn(Optional.of(root));
        when(commentRepository.saveAndFlush(any(Comment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userService.getUserBriefs(anyCollection())).thenReturn(List.of());

        CommentResponse response = service.createComment(studentId, lectureId, new CommentCreateRequest("Mình giúp", root.getId()));

        assertThat(response.parentId()).isEqualTo(root.getId());
    }

    @Test
    void createComment_cannotReplyToAReply() {
        stubAccess();
        Comment reply = comment(UUID.randomUUID(), lectureId, UUID.randomUUID());
        when(commentRepository.findById(reply.getId())).thenReturn(Optional.of(reply));

        assertThatThrownBy(() -> service.createComment(studentId, lectureId, new CommentCreateRequest("x", reply.getId())))
                .isInstanceOfSatisfying(BusinessException.class, e -> assertThat(e.getCode()).isEqualTo("VALIDATION_ERROR"));
        verify(commentRepository, never()).saveAndFlush(any());
    }

    @Test
    void createComment_cannotReplyToACommentOfAnotherLecture() {
        stubAccess();
        Comment foreign = comment(UUID.randomUUID(), UUID.randomUUID(), null);
        when(commentRepository.findById(foreign.getId())).thenReturn(Optional.of(foreign));

        assertThatThrownBy(() -> service.createComment(studentId, lectureId, new CommentCreateRequest("x", foreign.getId())))
                .isInstanceOfSatisfying(BusinessException.class, e -> assertThat(e.getCode()).isEqualTo("VALIDATION_ERROR"));
    }

    @Test
    void createComment_unknownParentIsNotFound() {
        stubAccess();
        UUID missing = UUID.randomUUID();
        when(commentRepository.findById(missing)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createComment(studentId, lectureId, new CommentCreateRequest("x", missing)))
                .isInstanceOfSatisfying(BusinessException.class, e -> assertThat(e.getCode()).isEqualTo("NOT_FOUND"));
    }

    @Test
    void updateComment_ofAnotherUserIsNotFound() {
        UUID id = UUID.randomUUID();
        when(commentRepository.findByIdAndUserId(id, studentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateComment(studentId, id, new CommentUpdateRequest("hack")))
                .isInstanceOfSatisfying(BusinessException.class, e -> assertThat(e.getCode()).isEqualTo("NOT_FOUND"));
    }

    @Test
    void updateComment_ownRootCommentChangesContentAndKeepsRepliesWithTheirAuthors() {
        Comment root = comment(UUID.randomUUID(), lectureId, null);
        UUID replier = UUID.randomUUID();
        Comment reply = Comment.builder().id(UUID.randomUUID()).lectureId(lectureId).userId(replier)
                .parentId(root.getId()).content("trả lời").build();
        when(commentRepository.findByIdAndUserId(root.getId(), studentId)).thenReturn(Optional.of(root));
        when(commentRepository.findByParentIdInOrderByCreatedAtAscIdAsc(List.of(root.getId()))).thenReturn(List.of(reply));
        when(userService.getUserBriefs(anyCollection())).thenReturn(List.of(
                new UserBriefResponse(studentId, "Tác giả", "a@x.y"), new UserBriefResponse(replier, "Người trả lời", "b@x.y")));

        CommentResponse response = service.updateComment(studentId, root.getId(), new CommentUpdateRequest("Đã sửa"));

        assertThat(root.getContent()).isEqualTo("Đã sửa");
        assertThat(response.replies()).hasSize(1);
        assertThat(response.replies().get(0).author().fullName()).isEqualTo("Người trả lời");
    }

    @Test
    void deleteComment_softDeletesOwnComment() {
        Comment own = comment(UUID.randomUUID(), lectureId, null);
        when(commentRepository.findByIdAndUserId(own.getId(), studentId)).thenReturn(Optional.of(own));

        service.deleteComment(studentId, own.getId());

        assertThat(own.getDeletedAt()).isNotNull();
        verify(commentRepository, never()).delete(any(Comment.class));
    }

    @Test
    void getComments_listsRootsNewestFirstWithRepliesOldestFirst() {
        stubAccess();
        Comment root = comment(UUID.randomUUID(), lectureId, null);
        Comment reply = Comment.builder().id(UUID.randomUUID()).lectureId(lectureId).userId(studentId)
                .parentId(root.getId()).content("trả lời").build();
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        when(commentRepository.findByLectureIdAndParentIdIsNull(eq(lectureId), pageable.capture()))
                .thenReturn(new PageImpl<>(List.of(root)));
        when(commentRepository.findByParentIdInOrderByCreatedAtAscIdAsc(List.of(root.getId()))).thenReturn(List.of(reply));
        when(userService.getUserBriefs(anyCollection())).thenReturn(List.of(new UserBriefResponse(studentId, "Học viên", "hv@x.y")));

        Page<CommentResponse> page = service.getComments(studentId, lectureId, PageRequestParams.of(null, null, null, null));

        assertThat(pageable.getValue().getSort().getOrderFor("createdAt").isDescending()).isTrue();
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).replies()).extracting(CommentResponse::content).containsExactly("trả lời");
        verify(accessValidator).requireLearningAccess(studentId, courseId);
    }

    @Test
    void getComments_noCommentsSkipsReplyQuery() {
        stubAccess();
        when(commentRepository.findByLectureIdAndParentIdIsNull(eq(lectureId), any(Pageable.class))).thenReturn(Page.empty());
        when(userService.getUserBriefs(anyCollection())).thenReturn(List.of());

        assertThat(service.getComments(studentId, lectureId, PageRequestParams.of(null, null, null, null))).isEmpty();
        verify(commentRepository, never()).findByParentIdInOrderByCreatedAtAscIdAsc(anyCollection());
    }

    private Comment comment(UUID id, UUID lecture, UUID parent) {
        return Comment.builder().id(id).lectureId(lecture).userId(studentId).parentId(parent).content("nội dung").build();
    }
}
