package com.elearning.learning.mapper;

import com.elearning.common.config.CommonMapperConfig;
import com.elearning.course.dto.response.CourseBriefResponse;
import com.elearning.learning.dto.response.CommentAuthorResponse;
import com.elearning.learning.dto.response.CommentResponse;
import com.elearning.learning.dto.response.CourseHistoryResponse;
import com.elearning.learning.dto.response.EnrolledStudentResponse;
import com.elearning.learning.dto.response.EnrollmentResponse;
import com.elearning.learning.entity.Comment;
import com.elearning.learning.entity.Enrollment;
import com.elearning.user.dto.response.UserBriefResponse;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Các response của learning ghép dữ liệu từ nhiều module nên đều viết bằng default method.
 */
@Mapper(config = CommonMapperConfig.class)
public interface LearningMapper {

    default EnrollmentResponse toEnrollmentResponse(Enrollment enrollment, CourseBriefResponse course,
                                                    String teacherName, int progress) {
        return new EnrollmentResponse(course.id(), course.code(), course.title(), course.imageUrl(), teacherName,
                course.startDate(), course.endDate(), progress, enrollment.getEnrolledAt());
    }

    default CourseHistoryResponse toCourseHistoryResponse(CourseBriefResponse course, long studentCount) {
        return new CourseHistoryResponse(course.id(), course.code(), course.title(), course.status(),
                course.startDate(), course.endDate(), studentCount);
    }

    // Người dùng null nghĩa là không tìm thấy hồ sơ (dữ liệu cũ), vẫn trả dòng ghi danh để không mất học viên khỏi danh sách.
    default EnrolledStudentResponse toEnrolledStudentResponse(Enrollment enrollment, UserBriefResponse user, int progress) {
        return new EnrolledStudentResponse(enrollment.getStudentId(), user == null ? null : user.fullName(),
                user == null ? null : user.email(), enrollment.getEnrolledAt(), progress);
    }

    default CommentResponse toCommentResponse(Comment comment, CommentAuthorResponse author, List<CommentResponse> replies) {
        return new CommentResponse(comment.getId(), comment.getLectureId(), comment.getParentId(), comment.getContent(),
                author, comment.getCreatedAt(), comment.getUpdatedAt(), replies);
    }
}
