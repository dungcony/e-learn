package com.elearning.learning.helper;

import com.elearning.course.dto.response.CourseLectureIds;
import com.elearning.course.service.LectureService;
import com.elearning.learning.repository.LectureCompletionRepository;
import com.elearning.learning.repository.StudentLectureView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProgressCalculatorTest {

    @Mock
    private LectureService lectureService;
    @Mock
    private LectureCompletionRepository completionRepository;
    @InjectMocks
    private ProgressCalculator calculator;

    private final UUID studentId = UUID.randomUUID();
    private final UUID course1 = UUID.randomUUID();
    private final UUID course2 = UUID.randomUUID();

    @Test
    void forStudent_roundsDownAndReturnsZeroForCourseWithoutLectures() {
        UUID l1 = UUID.randomUUID();
        when(lectureService.getLectureIds(List.of(course1, course2))).thenReturn(List.of(
                new CourseLectureIds(course1, List.of(l1, UUID.randomUUID(), UUID.randomUUID())),
                new CourseLectureIds(course2, List.of())));
        when(completionRepository.findCompletedLectureIds(studentId, List.of(course1, course2))).thenReturn(List.of(l1));

        Map<UUID, Integer> progress = calculator.forStudent(studentId, List.of(course1, course2));

        assertThat(progress).containsEntry(course1, 33).containsEntry(course2, 0);
    }

    @Test
    void forStudent_completionOfDeletedLectureIsNotCounted() {
        UUID alive = UUID.randomUUID();
        UUID deleted = UUID.randomUUID();
        when(lectureService.getLectureIds(List.of(course1)))
                .thenReturn(List.of(new CourseLectureIds(course1, List.of(alive))));
        when(completionRepository.findCompletedLectureIds(studentId, List.of(course1))).thenReturn(List.of(alive, deleted));

        assertThat(calculator.forStudent(studentId, List.of(course1))).containsEntry(course1, 100);
    }

    @Test
    void forStudent_emptyInputMakesNoQueries() {
        assertThat(calculator.forStudent(studentId, List.of())).isEmpty();
        verifyNoInteractions(lectureService, completionRepository);
    }

    @Test
    void forCourse_computesEachStudentSeparatelyIncludingThoseWithNoCompletion() {
        UUID s2 = UUID.randomUUID();
        UUID l1 = UUID.randomUUID();
        UUID l2 = UUID.randomUUID();
        when(lectureService.getLectureIds(List.of(course1))).thenReturn(List.of(new CourseLectureIds(course1, List.of(l1, l2))));
        when(completionRepository.findCompletions(course1, List.of(studentId, s2))).thenReturn(List.of(
                view(studentId, l1), view(studentId, l2), view(s2, UUID.randomUUID())));

        Map<UUID, Integer> progress = calculator.forCourse(course1, List.of(studentId, s2));

        assertThat(progress).containsEntry(studentId, 100).containsEntry(s2, 0);
    }

    @Test
    void forCourse_emptyStudentsMakesNoQueries() {
        assertThat(calculator.forCourse(course1, List.of())).isEmpty();
        verifyNoInteractions(lectureService, completionRepository);
    }

    private StudentLectureView view(UUID student, UUID lecture) {
        return new StudentLectureView() {
            public UUID getStudentId() { return student; }
            public UUID getLectureId() { return lecture; }
        };
    }
}
