package com.elearning.learning.service.impl;

import com.elearning.common.exception.BusinessException;
import com.elearning.common.exception.ErrorCode;
import com.elearning.common.response.PageRequestParams;
import com.elearning.course.service.LectureService;
import com.elearning.learning.dto.request.CommentCreateRequest;
import com.elearning.learning.dto.request.CommentUpdateRequest;
import com.elearning.learning.dto.response.CommentAuthorResponse;
import com.elearning.learning.dto.response.CommentResponse;
import com.elearning.learning.entity.Comment;
import com.elearning.learning.mapper.LearningMapper;
import com.elearning.learning.repository.CommentRepository;
import com.elearning.learning.service.CommentService;
import com.elearning.learning.validator.LearningAccessValidator;
import com.elearning.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private static final Map<String, String> SORTABLE_FIELDS = Map.of("created_at", "createdAt");

    private final CommentRepository commentRepository;
    private final LearningAccessValidator accessValidator;
    private final LectureService lectureService;
    private final UserService userService;
    private final LearningMapper learningMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponse> getComments(UUID studentId, UUID lectureId, PageRequestParams params) {
        requireAccessToLecture(studentId, lectureId);
        Page<Comment> roots = commentRepository.findByLectureIdAndParentIdIsNull(
                lectureId, params.toPageable(SORTABLE_FIELDS, "createdAt"));
        List<UUID> rootIds = roots.stream().map(Comment::getId).toList();
        Map<UUID, List<Comment>> repliesByParent = rootIds.isEmpty() ? Map.of()
                : commentRepository.findByParentIdInOrderByCreatedAtAscIdAsc(rootIds).stream()
                .collect(Collectors.groupingBy(Comment::getParentId));

        Set<UUID> authorIds = new HashSet<>();
        roots.forEach(c -> authorIds.add(c.getUserId()));
        repliesByParent.values().forEach(list -> list.forEach(c -> authorIds.add(c.getUserId())));
        Map<UUID, CommentAuthorResponse> authors = authors(authorIds);

        return roots.map(root -> learningMapper.toCommentResponse(root, authors.get(root.getUserId()),
                repliesByParent.getOrDefault(root.getId(), List.of()).stream()
                        .map(r -> learningMapper.toCommentResponse(r, authors.get(r.getUserId()), List.of())).toList()));
    }

    @Override
    @Transactional
    public CommentResponse createComment(UUID studentId, UUID lectureId, CommentCreateRequest request) {
        requireAccessToLecture(studentId, lectureId);
        if (request.parentId() != null) {
            Comment parent = commentRepository.findById(request.parentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy bình luận được trả lời."));
            if (!parent.getLectureId().equals(lectureId) || parent.getParentId() != null) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                        "Chỉ được trả lời bình luận gốc của cùng bài giảng.");
            }
        }
        // Flush để created_at, updated_at (gán lúc flush) có giá trị trong response.
        Comment comment = commentRepository.saveAndFlush(Comment.builder().id(UUID.randomUUID()).lectureId(lectureId)
                .userId(studentId).parentId(request.parentId()).content(request.content()).build());
        return learningMapper.toCommentResponse(comment, authors(List.of(studentId)).get(studentId), List.of());
    }

    @Override
    @Transactional
    public CommentResponse updateComment(UUID studentId, UUID id, CommentUpdateRequest request) {
        Comment comment = findOwned(studentId, id);
        comment.setContent(request.content());
        commentRepository.flush();
        List<Comment> replies = comment.getParentId() != null ? List.of()
                : commentRepository.findByParentIdInOrderByCreatedAtAscIdAsc(List.of(id));

        Set<UUID> authorIds = new HashSet<>(List.of(studentId));
        replies.forEach(r -> authorIds.add(r.getUserId()));
        Map<UUID, CommentAuthorResponse> authors = authors(authorIds);
        return learningMapper.toCommentResponse(comment, authors.get(studentId),
                replies.stream().map(r -> learningMapper.toCommentResponse(r, authors.get(r.getUserId()), List.of())).toList());
    }

    @Override
    @Transactional
    public void deleteComment(UUID studentId, UUID id) {
        findOwned(studentId, id).setDeletedAt(Instant.now());
    }

    private void requireAccessToLecture(UUID studentId, UUID lectureId) {
        accessValidator.requireLearningAccess(studentId, lectureService.getCourseIdOfLecture(lectureId));
    }

    private Comment findOwned(UUID studentId, UUID id) {
        return commentRepository.findByIdAndUserId(id, studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy bình luận."));
    }

    private Map<UUID, CommentAuthorResponse> authors(Collection<UUID> userIds) {
        Map<UUID, CommentAuthorResponse> authors = new HashMap<>();
        userService.getUserBriefs(List.copyOf(userIds))
                .forEach(b -> authors.put(b.id(), new CommentAuthorResponse(b.id(), b.fullName())));
        return authors;
    }
}
