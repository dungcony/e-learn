package com.elearning.learning.helper;

import com.elearning.course.dto.response.CourseLectureIds;
import com.elearning.course.service.LectureService;
import com.elearning.learning.repository.LectureCompletionRepository;
import com.elearning.learning.repository.StudentLectureView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Tính tiến độ khóa học khi đọc: số bài giảng đã xác nhận hoàn thành / tổng số bài giảng hiện có × 100, làm tròn xuống.
 * Chỉ đếm bài giảng còn tồn tại nên xóa bài giảng không làm tiến độ vượt 100; khóa học chưa có bài giảng thì tiến độ 0.
 */
@Component
@RequiredArgsConstructor
public class ProgressCalculator {

    private final LectureService lectureService;
    private final LectureCompletionRepository completionRepository;

    /**
     * Tiến độ của một học viên trên nhiều khóa học.
     *
     * @return map từ id khóa học tới tiến độ; có đủ mọi id đầu vào
     */
    public Map<UUID, Integer> forStudent(UUID studentId, Collection<UUID> courseIds) {
        if (courseIds.isEmpty()) {
            return Map.of();
        }
        Set<UUID> completed = new HashSet<>(completionRepository.findCompletedLectureIds(studentId, courseIds));
        Map<UUID, Integer> result = new HashMap<>();
        for (CourseLectureIds course : lectureService.getLectureIds(courseIds)) {
            result.put(course.courseId(), percent(course.lectureIds(), completed));
        }
        return result;
    }

    /**
     * Tiến độ của nhiều học viên trên một khóa học.
     *
     * @return map từ id học viên tới tiến độ; có đủ mọi id đầu vào
     */
    public Map<UUID, Integer> forCourse(UUID courseId, Collection<UUID> studentIds) {
        if (studentIds.isEmpty()) {
            return Map.of();
        }
        List<UUID> lectureIds = lectureService.getLectureIds(List.of(courseId)).stream()
                .findFirst().map(CourseLectureIds::lectureIds).orElse(List.of());
        Map<UUID, Set<UUID>> completedByStudent = new HashMap<>();
        for (StudentLectureView view : completionRepository.findCompletions(courseId, studentIds)) {
            completedByStudent.computeIfAbsent(view.getStudentId(), k -> new HashSet<>()).add(view.getLectureId());
        }
        Map<UUID, Integer> result = new HashMap<>();
        for (UUID studentId : studentIds) {
            result.put(studentId, percent(lectureIds, completedByStudent.getOrDefault(studentId, Set.of())));
        }
        return result;
    }

    private int percent(List<UUID> lectureIds, Set<UUID> completed) {
        if (lectureIds.isEmpty()) {
            return 0;
        }
        long done = lectureIds.stream().filter(completed::contains).count();
        return (int) (done * 100 / lectureIds.size());
    }
}
