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
 * Ghi danh của học viên vào khóa học (UC016), bảng {@code enrollments}. Không xóa mềm: học viên hoặc khóa học đã xóa
 * vẫn giữ bản ghi để lịch sử khóa học (UC014) còn đủ. Tiến độ không lưu ở đây mà tính từ {@link LectureCompletion}.
 */
@Entity
@Table(name = "enrollments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Enrollment extends AssignedIdEntity {

    @Id
    private UUID id;

    @Column(name = "student_id", nullable = false, updatable = false)
    private UUID studentId;

    @Column(name = "course_id", nullable = false, updatable = false)
    private UUID courseId;

    @Column(name = "enrolled_at", nullable = false, updatable = false)
    private Instant enrolledAt;
}
