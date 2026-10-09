package com.elearning.course.entity;

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
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.UUID;

/**
 * Đáp án của một {@link Question}, bảng {@code answers}. Mỗi câu hỏi có 4 đáp án và đúng một đáp án {@code correct}.
 */
@Entity
@Table(name = "answers")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Answer extends AssignedIdEntity {

    @Id
    private UUID id;

    @Column(name = "question_id", nullable = false, updatable = false)
    private UUID questionId;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    // Thứ tự đáp án trong câu hỏi, từ 0.
    @Column(nullable = false)
    private int position;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
