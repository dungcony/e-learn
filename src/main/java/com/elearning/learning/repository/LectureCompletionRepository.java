package com.elearning.learning.repository;

import com.elearning.learning.entity.LectureCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface LectureCompletionRepository extends JpaRepository<LectureCompletion, UUID> {

    boolean existsByStudentIdAndLectureId(UUID studentId, UUID lectureId);

    /**
     * Ghi hoàn thành một bài giảng, bỏ qua nếu đã có. Gọi lại hoặc hai request song song đều thành công và không tạo
     * bản ghi trùng; dùng {@code ON CONFLICT} thay vì bắt lỗi unique vì lỗi đó làm transaction đang chạy hỏng.
     */
    @Modifying
    @Query(value = "INSERT INTO lecture_completions (id, student_id, course_id, lecture_id, completed_at) "
            + "VALUES (:id, :studentId, :courseId, :lectureId, :completedAt) "
            + "ON CONFLICT (student_id, lecture_id) DO NOTHING", nativeQuery = true)
    int insertIfAbsent(@Param("id") UUID id, @Param("studentId") UUID studentId, @Param("courseId") UUID courseId,
                       @Param("lectureId") UUID lectureId, @Param("completedAt") Instant completedAt);

    @Query("SELECT lc.lectureId FROM LectureCompletion lc WHERE lc.studentId = :studentId AND lc.courseId IN :courseIds")
    List<UUID> findCompletedLectureIds(@Param("studentId") UUID studentId, @Param("courseIds") Collection<UUID> courseIds);

    @Query("SELECT lc.studentId AS studentId, lc.lectureId AS lectureId FROM LectureCompletion lc "
            + "WHERE lc.courseId = :courseId AND lc.studentId IN :studentIds")
    List<StudentLectureView> findCompletions(@Param("courseId") UUID courseId, @Param("studentIds") Collection<UUID> studentIds);
}
