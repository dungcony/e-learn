package com.elearning.course.mapper;

import com.elearning.common.config.CommonMapperConfig;
import com.elearning.course.dto.request.CourseCreateRequest;
import com.elearning.course.dto.request.CourseUpdateRequest;
import com.elearning.course.dto.response.CategoryRefResponse;
import com.elearning.course.dto.response.CourseBriefResponse;
import com.elearning.course.dto.response.CourseDetailResponse;
import com.elearning.course.dto.response.CourseSummaryResponse;
import com.elearning.course.dto.response.LectureRefResponse;
import com.elearning.course.dto.response.TeacherRefResponse;
import com.elearning.course.entity.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(config = CommonMapperConfig.class)
public interface CourseMapper {

    @Mapping(target = "categoryName", source = "categoryName")
    @Mapping(target = "teacherName", source = "teacherName")
    CourseSummaryResponse toSummaryResponse(Course course, String categoryName, String teacherName);

    CourseBriefResponse toBriefResponse(Course course);

    // Ghép nhiều nguồn có cùng tên thuộc tính id nên viết tay thay vì để MapStruct đoán.
    default CourseDetailResponse toDetailResponse(Course course, CategoryRefResponse category,
                                                  TeacherRefResponse teacher, List<LectureRefResponse> lectures) {
        return new CourseDetailResponse(course.getId(), course.getCode(), course.getTitle(), course.getDescription(),
                course.getPrice(), course.getStartDate(), course.getEndDate(), course.getStatus(),
                course.getImageUrl(), course.getReferenceMaterials(), category, teacher, lectures);
    }

    // id, code, ảnh, giảng viên chủ và mốc thời gian do service gán; giá trống do service đổi về 0.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "teacherId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Course toNewCourse(CourseCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "teacherId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateCourse(CourseUpdateRequest request, @MappingTarget Course course);
}
