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

import java.time.Instant;
import java.util.UUID;

/**
 * Học viên xác nhận đã học xong một bài giảng (UC016 bước 10–11), bảng {@code lecture_completions}.
 */
@Entity
@Table(name = "lecture_completions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LectureCompletion extends AssignedIdEntity {

    @Id
    private UUID id;

    @Column(name = "student_id", nullable = false, updatable = false)
    private UUID studentId;

    // Lưu kèm khóa học để đếm tiến độ theo khóa mà không phải hỏi module course từng bài giảng.
    @Column(name = "course_id", nullable = false, updatable = false)
    private UUID courseId;

    @Column(name = "lecture_id", nullable = false, updatable = false)
    private UUID lectureId;

    @Column(name = "completed_at", nullable = false, updatable = false)
    private Instant completedAt;
}
