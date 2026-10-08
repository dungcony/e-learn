package com.elearning.info.entity;

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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Câu hỏi thường gặp do Quản trị viên quản lý (UC013), bảng {@code faqs}.
 */
@Entity
@Table(name = "faqs")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Faq extends AssignedIdEntity {

    @Id
    private UUID id;

    @Column(nullable = false, columnDefinition = "text")
    private String question;

    @Column(nullable = false, columnDefinition = "text")
    private String answer;

    @Column(name = "created_by", nullable = false, updatable = false)
    private UUID createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
