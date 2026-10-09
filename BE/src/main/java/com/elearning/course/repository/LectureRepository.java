package com.elearning.course.repository;

import com.elearning.course.entity.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LectureRepository extends JpaRepository<Lecture, UUID>, JpaSpecificationExecutor<Lecture> {

    Optional<Lecture> findByIdAndCourseId(UUID id, UUID courseId);

    // Bài giảng thuộc khóa học chưa xóa của giảng viên này; khóa học đã xóa thì bài giảng coi như không truy cập được.
    @Query("""
            SELECT l FROM Lecture l
            WHERE l.id = :id
              AND EXISTS (SELECT 1 FROM Course c WHERE c.id = l.courseId AND c.teacherId = :teacherId)
            """)
    Optional<Lecture> findByIdAndTeacherId(@Param("id") UUID id, @Param("teacherId") UUID teacherId);

    List<Lecture> findByCourseIdOrderByCreatedAtAscIdAsc(UUID courseId);

    @Query("SELECT l.courseId AS courseId, l.id AS id FROM Lecture l WHERE l.courseId IN :courseIds")
    List<LectureIdView> findLectureIdsByCourseIds(@Param("courseIds") Collection<UUID> courseIds);
}
