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

import java.util.UUID;

/**
 * Đáp án học viên chọn cho một câu hỏi, bảng {@code submission_answers}. Trỏ tới câu hỏi và đáp án bằng id nên vẫn
 * đọc được khi giảng viên đã thay câu hỏi (câu cũ bị xóa mềm).
 */
@Entity
@Table(name = "submission_answers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionAnswer extends AssignedIdEntity {

    @Id
    private UUID id;

    @Column(name = "submission_id", nullable = false, updatable = false)
    private UUID submissionId;

    @Column(name = "question_id", nullable = false, updatable = false)
    private UUID questionId;

    @Column(name = "answer_id", nullable = false)
    private UUID answerId;

    // Chỉ ghi khi nộp bài; null khi còn DRAFT.
    @Column(name = "is_correct")
    private Boolean correct;
}
