package com.elearning.learning.repository;

import com.elearning.learning.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    // Kiểm tra quyền sở hữu ngay trong câu truy vấn (rule 2.9): bình luận của người khác coi như không tồn tại.
    Optional<Comment> findByIdAndUserId(UUID id, UUID userId);

    Page<Comment> findByLectureIdAndParentIdIsNull(UUID lectureId, Pageable pageable);

    List<Comment> findByParentIdInOrderByCreatedAtAscIdAsc(Collection<UUID> parentIds);
}
