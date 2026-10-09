package com.elearning.course.mapper;

import com.elearning.common.config.CommonMapperConfig;
import com.elearning.course.dto.request.LectureCreateRequest;
import com.elearning.course.dto.request.LectureUpdateRequest;
import com.elearning.course.dto.response.ExerciseSummaryResponse;
import com.elearning.course.dto.response.LectureDetailResponse;
import com.elearning.course.dto.response.LectureRefResponse;
import com.elearning.course.dto.response.LectureSummaryResponse;
import com.elearning.course.entity.Lecture;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(config = CommonMapperConfig.class)
public interface LectureMapper {

    LectureSummaryResponse toSummaryResponse(Lecture lecture);

    LectureRefResponse toRefResponse(Lecture lecture);

    default LectureDetailResponse toDetailResponse(Lecture lecture, List<ExerciseSummaryResponse> exercises) {
        return new LectureDetailResponse(lecture.getId(), lecture.getCourseId(), lecture.getTitle(),
                lecture.getDescription(), lecture.getContentUrl(), lecture.getCreatedBy(), exercises);
    }

    // id, khóa học, người tạo và mốc thời gian do service gán.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "courseId", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Lecture toNewLecture(LectureCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "courseId", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateLecture(LectureUpdateRequest request, @MappingTarget Lecture lecture);
}
