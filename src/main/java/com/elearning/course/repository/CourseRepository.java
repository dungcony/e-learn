package com.elearning.course.repository;

import com.elearning.course.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CourseRepository extends JpaRepository<Course, UUID>, JpaSpecificationExecutor<Course> {

    // Kiểm tra quyền sở hữu ngay trong câu truy vấn (rule 2.9): khóa học của giảng viên khác coi như không tồn tại.
    Optional<Course> findByIdAndTeacherId(UUID id, UUID teacherId);

    // Native để tính cả khóa học đã xóa mềm: unique constraint của code vẫn áp dụng với chúng.
    @Query(value = "SELECT EXISTS (SELECT 1 FROM courses WHERE code = :code)", nativeQuery = true)
    boolean existsByCode(@Param("code") String code);

    // Chỉ tính khóa học chưa xóa mềm (@SQLRestriction).
    boolean existsByCategoryId(UUID categoryId);
}
