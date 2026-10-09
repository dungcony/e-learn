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
 * Câu hỏi trắc nghiệm của một {@link Exercise}, bảng {@code questions}. Khi giảng viên sửa bài tập, câu hỏi cũ bị xóa
 * mềm và tạo câu mới, nên bài làm cũ của học viên vẫn trỏ được tới câu cũ.
 */
@Entity
@Table(name = "questions")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Question extends AssignedIdEntity {

    @Id
    private UUID id;

    @Column(name = "exercise_id", nullable = false, updatable = false)
    private UUID exerciseId;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    // Thứ tự câu trong bài tập, từ 0.
    @Column(nullable = false)
    private int position;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
