package com.elearning.course.dto.response;

import java.util.List;
import java.util.UUID;

/**
 * Chi tiết bài giảng cho giảng viên chủ khóa học, kèm danh sách bài tập.
 */
public record LectureDetailResponse(
        UUID id,
        UUID courseId,
        String title,
        String description,
        String contentUrl,
        UUID createdBy,
        List<ExerciseSummaryResponse> exercises
) {
}
