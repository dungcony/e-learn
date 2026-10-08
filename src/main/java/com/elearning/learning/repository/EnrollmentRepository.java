package com.elearning.learning.repository;

import com.elearning.learning.entity.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    boolean existsByStudentIdAndCourseId(UUID studentId, UUID courseId);

    List<Enrollment> findByStudentId(UUID studentId);

    Page<Enrollment> findByCourseId(UUID courseId, Pageable pageable);

    @Query("SELECT e.courseId AS courseId, COUNT(e) AS studentCount FROM Enrollment e "
            + "WHERE e.courseId IN :courseIds GROUP BY e.courseId")
    List<CourseStudentCount> countByCourseIds(@Param("courseIds") Collection<UUID> courseIds);
}
