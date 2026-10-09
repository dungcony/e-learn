package com.elearning.learning.entity;

import com.elearning.common.abstracts.AssignedIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Bình luận thảo luận dưới bài giảng (UC016), bảng {@code comments}. Chỉ trả lời một cấp: {@code parentId} nếu có
 * luôn là một bình luận gốc của cùng bài giảng.
 */
@Entity
@Table(name = "comments")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment extends AssignedIdEntity {

    @Id
    private UUID id;

    @Column(name = "lecture_id", nullable = false, updatable = false)
    private UUID lectureId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    // null là bình luận gốc.
    @Column(name = "parent_id", updatable = false)
    private UUID parentId;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
