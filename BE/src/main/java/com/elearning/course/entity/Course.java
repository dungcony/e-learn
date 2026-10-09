package com.elearning.course.entity;

import com.elearning.common.abstracts.AssignedIdEntity;
import com.elearning.course.enums.CourseStatus;
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
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Khóa học (UC009), bảng {@code courses}. {@code teacherId} trỏ sang người dùng của module {@code user} chỉ bằng id.
 */
@Entity
@Table(name = "courses")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Course extends AssignedIdEntity {

    @Id
    private UUID id;

    // Hệ thống sinh, dạng CO + 6 chữ số, không đổi sau khi tạo.
    @Column(nullable = false, updatable = false)
    private String code;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    // Đơn vị VND.
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseStatus status;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "reference_materials", columnDefinition = "text")
    private String referenceMaterials;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(name = "teacher_id", nullable = false, updatable = false)
    private UUID teacherId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
