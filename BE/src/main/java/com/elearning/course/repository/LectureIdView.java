package com.elearning.course.repository;

import java.util.UUID;

// Projection chỉ gồm khóa học và id bài giảng, để không tải mô tả của mọi bài giảng khi chỉ cần đếm.
public interface LectureIdView {

    UUID getCourseId();

    UUID getId();
}
