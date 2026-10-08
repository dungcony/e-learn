package com.elearning.course.service.impl;

import com.elearning.common.exception.BusinessException;
import com.elearning.common.storage.FileStorageService;
import com.elearning.course.dto.request.CourseCreateRequest;
import com.elearning.course.dto.request.CourseUpdateRequest;
import com.elearning.course.dto.response.CourseDetailResponse;
import com.elearning.course.entity.Category;
import com.elearning.course.entity.Course;
import com.elearning.course.enums.CourseStatus;
import com.elearning.course.mapper.CategoryMapper;
import com.elearning.course.mapper.CourseMapper;
import com.elearning.course.mapper.LectureMapper;
import com.elearning.course.repository.CategoryRepository;
import com.elearning.course.repository.CourseRepository;
import com.elearning.course.repository.LectureRepository;
import com.elearning.course.validator.CourseValidator;
import com.elearning.user.dto.response.UserBriefResponse;
import com.elearning.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceImplTest {

    @Mock
    private CourseRepository courseRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private LectureRepository lectureRepository;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private UserService userService;

    private CourseServiceImpl service;

    private final UUID teacherId = UUID.randomUUID();
    private final UUID categoryId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new CourseServiceImpl(courseRepository, categoryRepository, lectureRepository,
                Mappers.getMapper(CourseMapper.class), Mappers.getMapper(CategoryMapper.class),
                Mappers.getMapper(LectureMapper.class), new CourseValidator(), fileStorageService, userService);
    }

    // ---- createCourse ----

    @Test
    void createCourse_assignsCodeOwnerAndDefaultPrice() {
        stubDetailLookups();
        when(categoryRepository.existsById(categoryId)).thenReturn(true);
        when(courseRepository.existsByCode(anyString())).thenReturn(false);
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));

        CourseDetailResponse response = service.createCourse(teacherId, createRequest(null));

        ArgumentCaptor<Course> saved = ArgumentCaptor.forClass(Course.class);
        verify(courseRepository).save(saved.capture());
        assertThat(saved.getValue().getCode()).matches("CO\\d{6}");
        assertThat(saved.getValue().getTeacherId()).isEqualTo(teacherId);
        assertThat(saved.getValue().getPrice()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(saved.getValue().getId()).isNotNull();
        assertThat(response.teacher().fullName()).isEqualTo("Giảng viên");
        assertThat(response.category().name()).isEqualTo("Toán");
    }

    @Test
    void createCourse_retriesWhenGeneratedCodeIsTaken() {
        stubDetailLookups();
        when(categoryRepository.existsById(categoryId)).thenReturn(true);
        when(courseRepository.existsByCode(anyString())).thenReturn(true, true, false);
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));

        service.createCourse(teacherId, createRequest(BigDecimal.TEN));

        verify(courseRepository, org.mockito.Mockito.times(3)).existsByCode(anyString());
    }

    @Test
    void createCourse_invalidDateRangeFailsBeforeAnyLookup() {
        CourseCreateRequest request = new CourseCreateRequest("T", "D", LocalDate.of(2020, 4, 15),
                LocalDate.of(2020, 4, 15), CourseStatus.PUBLIC, categoryId, null, null);

        assertThatThrownBy(() -> service.createCourse(teacherId, request))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo("COURSE_DATE_RANGE_INVALID"));
        verify(categoryRepository, never()).existsById(any());
        verify(courseRepository, never()).save(any());
    }

    @Test
    void createCourse_unknownCategoryIsNotFound() {
        when(categoryRepository.existsById(categoryId)).thenReturn(false);

        assertThatThrownBy(() -> service.createCourse(teacherId, createRequest(null)))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo("NOT_FOUND"));
        verify(courseRepository, never()).save(any());
    }

    // ---- getCourse: khóa PRIVATE bị ẩn ----

    @Test
    void getCourse_publicCourseIsVisibleToGuest() {
        Course course = course(CourseStatus.PUBLIC);
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        stubDetailLookups();

        assertThat(service.getCourse(null, false, course.getId()).id()).isEqualTo(course.getId());
    }

    @Test
    void getCourse_privateCourseIsHiddenFromGuestStudentAndOtherTeacher() {
        Course course = course(CourseStatus.PRIVATE);
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        for (UUID viewer : new UUID[]{null, UUID.randomUUID()}) {
            assertThatThrownBy(() -> service.getCourse(viewer, false, course.getId()))
                    .isInstanceOfSatisfying(BusinessException.class,
                            e -> assertThat(e.getCode()).isEqualTo("NOT_FOUND"));
        }
    }

    @Test
    void getCourse_privateCourseIsVisibleToOwnerAndAdmin() {
        Course course = course(CourseStatus.PRIVATE);
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        stubDetailLookups();

        assertThat(service.getCourse(teacherId, false, course.getId())).isNotNull();
        assertThat(service.getCourse(UUID.randomUUID(), true, course.getId())).isNotNull();
    }

    @Test
    void getCourse_unknownCourseIsNotFound() {
        UUID id = UUID.randomUUID();
        when(courseRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getCourse(null, false, id)).isInstanceOf(BusinessException.class);
    }

    // ---- update / delete / image ----

    @Test
    void updateCourse_otherTeachersCourseIsNotFound() {
        UUID id = UUID.randomUUID();
        when(courseRepository.findByIdAndTeacherId(id, teacherId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateCourse(teacherId, id, updateRequest()))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo("NOT_FOUND"));
    }

    @Test
    void updateCourse_replacesFieldsButKeepsCodeAndOwner() {
        Course course = course(CourseStatus.PUBLIC);
        String code = course.getCode();
        when(courseRepository.findByIdAndTeacherId(course.getId(), teacherId)).thenReturn(Optional.of(course));
        when(categoryRepository.existsById(categoryId)).thenReturn(true);
        stubDetailLookups();

        service.updateCourse(teacherId, course.getId(), updateRequest());

        assertThat(course.getTitle()).isEqualTo("Tên mới");
        assertThat(course.getStatus()).isEqualTo(CourseStatus.PRIVATE);
        assertThat(course.getCode()).isEqualTo(code);
        assertThat(course.getTeacherId()).isEqualTo(teacherId);
        assertThat(course.getPrice()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void deleteCourse_softDeletes() {
        Course course = course(CourseStatus.PUBLIC);
        when(courseRepository.findByIdAndTeacherId(course.getId(), teacherId)).thenReturn(Optional.of(course));

        service.deleteCourse(teacherId, course.getId());

        assertThat(course.getDeletedAt()).isNotNull();
        verify(courseRepository, never()).delete(any(Course.class));
    }

    @Test
    void updateCourseImage_storesUnderCoursesDirectory() {
        Course course = course(CourseStatus.PUBLIC);
        MockMultipartFile file = new MockMultipartFile("file", "c.png", "image/png", new byte[]{1});
        when(courseRepository.findByIdAndTeacherId(course.getId(), teacherId)).thenReturn(Optional.of(course));
        when(fileStorageService.storeImage(file, "courses")).thenReturn("/files/courses/c.png");
        stubDetailLookups();

        CourseDetailResponse response = service.updateCourseImage(teacherId, course.getId(), file);

        assertThat(response.imageUrl()).isEqualTo("/files/courses/c.png");
    }

    @Test
    void getCourseBriefs_emptyInputSkipsQuery() {
        assertThat(service.getCourseBriefs(List.of())).isEmpty();
        verify(courseRepository, never()).findAllById(any());
    }

    private void stubDetailLookups() {
        lenient().when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(Category.builder().id(categoryId).name("Toán").createdBy(teacherId).build()));
        lenient().when(userService.getUserBriefs(any())).thenReturn(
                List.of(new UserBriefResponse(teacherId, "Giảng viên", "gv@example.com")));
        lenient().when(lectureRepository.findByCourseIdOrderByCreatedAtAscIdAsc(any())).thenReturn(List.of());
    }


    private CourseCreateRequest createRequest(BigDecimal price) {
        return new CourseCreateRequest("Đại số", "Mô tả", LocalDate.of(2020, 4, 15), LocalDate.of(2020, 4, 30),
                CourseStatus.PUBLIC, categoryId, price, null);
    }

    private CourseUpdateRequest updateRequest() {
        return new CourseUpdateRequest("Tên mới", "Mô tả mới", LocalDate.of(2021, 1, 1), LocalDate.of(2021, 2, 1),
                CourseStatus.PRIVATE, categoryId, null, null);
    }

    private Course course(CourseStatus status) {
        return Course.builder().id(UUID.randomUUID()).code("CO123456").title("Cũ").description("Mô tả")
                .price(BigDecimal.ONE).startDate(LocalDate.of(2020, 4, 15)).endDate(LocalDate.of(2020, 4, 30))
                .status(status).categoryId(categoryId).teacherId(teacherId).build();
    }
}
