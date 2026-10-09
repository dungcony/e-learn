package com.elearning.course.repository;

import com.elearning.common.util.LikeUtils;
import com.elearning.course.entity.Lecture;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.UUID;

public final class LectureSpecification {

    private LectureSpecification() {
    }

    // Bài giảng của một khóa học, tên chứa chuỗi (Bảng 2-15); tên trống thì không lọc theo tên.
    public static Specification<Lecture> search(UUID courseId, String name) {
        return (root, query, cb) -> {
            var inCourse = cb.equal(root.get("courseId"), courseId);
            if (!StringUtils.hasText(name)) {
                return inCourse;
            }
            return cb.and(inCourse, cb.like(cb.lower(root.get("title")), LikeUtils.contains(name), LikeUtils.ESCAPE));
        };
    }
}
