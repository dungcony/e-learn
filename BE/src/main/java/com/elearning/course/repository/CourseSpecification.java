package com.elearning.course.repository;

import com.elearning.common.util.LikeUtils;
import com.elearning.course.dto.request.CourseSearchRequest;
import com.elearning.course.entity.Course;
import com.elearning.course.enums.CourseStatus;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Điều kiện tìm kiếm khóa học (UC007, UC009): các tiêu chí đã nhập được AND với nhau.
 */
public final class CourseSpecification {

    private CourseSpecification() {
    }

    /**
     * @param teacherId   chỉ lấy khóa học của giảng viên này; null thì lấy của mọi giảng viên
     * @param forcedStatus ép trạng thái (danh sách công khai luôn là {@code PUBLIC}); null thì dùng
     *                     {@code request.status()}
     */
    public static Specification<Course> search(UUID teacherId, CourseStatus forcedStatus, CourseSearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (teacherId != null) {
                predicates.add(cb.equal(root.get("teacherId"), teacherId));
            }
            CourseStatus status = forcedStatus != null ? forcedStatus : request.status();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            addContains(predicates, cb, root.get("code"), request.code());
            addContains(predicates, cb, root.get("title"), request.name());
            if (request.price() != null) {
                predicates.add(cb.equal(root.get("price"), request.price()));
            }
            if (request.startDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), request.startDate()));
            }
            if (request.endDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("endDate"), request.endDate()));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static void addContains(List<Predicate> predicates, CriteriaBuilder cb,
                                    Expression<String> field, String value) {
        if (StringUtils.hasText(value)) {
            predicates.add(cb.like(cb.lower(field), LikeUtils.contains(value), LikeUtils.ESCAPE));
        }
    }
}
