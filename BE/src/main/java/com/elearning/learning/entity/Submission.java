package com.elearning.learning.entity;

import com.elearning.common.abstracts.AssignedIdEntity;
import com.elearning.learning.enums.SubmissionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Bài làm của học viên cho một bài tập (UC016), bảng {@code submissions}; mỗi học viên một bài làm cho mỗi bài tập.
 */
@Entity
@Table(name = "submissions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Submission extends AssignedIdEntity {

    @Id
    private UUID id;

    @Column(name = "student_id", nullable = false, updatable = false)
    private UUID studentId;

    @Column(name = "exercise_id", nullable = false, updatable = false)
    private UUID exerciseId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubmissionStatus status;

    // Số câu đúng; null khi chưa nộp.
    private Integer score;

    // Số câu của bài tập lúc nộp; null khi chưa nộp.
    @Column(name = "total_questions")
    private Integer totalQuestions;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
