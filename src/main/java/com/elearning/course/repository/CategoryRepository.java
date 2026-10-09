package com.elearning.course.repository;

import com.elearning.course.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID>, JpaSpecificationExecutor<Category> {

    // Kiểm tra quyền sở hữu ngay trong câu truy vấn (rule 2.9): thể loại của giảng viên khác coi như không tồn tại.
    Optional<Category> findByIdAndCreatedBy(UUID id, UUID createdBy);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);
}
