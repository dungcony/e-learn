package com.elearning.course.service.impl;

import com.elearning.common.exception.BusinessException;
import com.elearning.common.exception.ErrorCode;
import com.elearning.common.response.PageRequestParams;
import com.elearning.common.storage.FileStorageService;
import com.elearning.course.dto.request.CourseCreateRequest;
import com.elearning.course.dto.request.CourseSearchRequest;
import com.elearning.course.dto.request.CourseUpdateRequest;
import com.elearning.course.dto.response.CategoryRefResponse;
import com.elearning.course.dto.response.CourseBriefResponse;
import com.elearning.course.dto.response.CourseDetailResponse;
import com.elearning.course.dto.response.CourseSummaryResponse;
import com.elearning.course.dto.response.LectureRefResponse;
import com.elearning.course.dto.response.TeacherRefResponse;
import com.elearning.course.entity.Category;
import com.elearning.course.entity.Course;
import com.elearning.course.enums.CourseStatus;
import com.elearning.course.mapper.CategoryMapper;
import com.elearning.course.mapper.CourseMapper;
import com.elearning.course.mapper.LectureMapper;
import com.elearning.course.repository.CategoryRepository;
import com.elearning.course.repository.CourseRepository;
import com.elearning.course.repository.CourseSpecification;
import com.elearning.course.repository.LectureRepository;
import com.elearning.course.service.CourseService;
import com.elearning.course.validator.CourseValidator;
import com.elearning.user.dto.response.UserBriefResponse;
import com.elearning.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private static final Map<String, String> SORTABLE_FIELDS = Map.of(
            "created_at", "createdAt", "title", "title", "start_date", "startDate", "end_date", "endDate", "price", "price");

    private static final int CODE_GENERATION_ATTEMPTS = 20;

    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final LectureRepository lectureRepository;
    private final CourseMapper courseMapper;
    private final CategoryMapper categoryMapper;
    private final LectureMapper lectureMapper;
    private final CourseValidator courseValidator;
    private final FileStorageService fileStorageService;
    private final UserService userService;

    @Override
    @Transactional(readOnly = true)
    public Page<CourseSummaryResponse> searchPublicCourses(CourseSearchRequest request, PageRequestParams params) {
        return toSummaryPage(courseRepository.findAll(
                CourseSpecification.search(null, CourseStatus.PUBLIC, request), pageable(params)));
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDetailResponse getCourse(UUID viewerId, boolean viewerIsAdmin, UUID id) {
        Course course = courseRepository.findById(id).orElseThrow(this::courseNotFound);
        boolean isOwner = viewerId != null && viewerId.equals(course.getTeacherId());
        if (course.getStatus() == CourseStatus.PRIVATE && !isOwner && !viewerIsAdmin) {
            throw courseNotFound();
        }
        return toDetail(course);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseSummaryResponse> searchTeacherCourses(UUID teacherId, CourseSearchRequest request,
                                                            PageRequestParams params) {
        return toSummaryPage(courseRepository.findAll(
                CourseSpecification.search(teacherId, null, request), pageable(params)));
    }

    @Override
    @Transactional
    public CourseDetailResponse createCourse(UUID teacherId, CourseCreateRequest request) {
        courseValidator.validateDateRange(request.startDate(), request.endDate());
        requireCategory(request.categoryId());
        Course course = courseMapper.toNewCourse(request);
        course.setId(UUID.randomUUID());
        course.setCode(generateCode());
        course.setTeacherId(teacherId);
        defaultPrice(course);
        return toDetail(courseRepository.save(course));
    }

    @Override
    @Transactional
    public CourseDetailResponse updateCourse(UUID teacherId, UUID id, CourseUpdateRequest request) {
        Course course = findOwned(teacherId, id);
        courseValidator.validateDateRange(request.startDate(), request.endDate());
        requireCategory(request.categoryId());
        courseMapper.updateCourse(request, course);
        defaultPrice(course);
        return toDetail(course);
    }

    @Override
    @Transactional
    public CourseDetailResponse updateCourseImage(UUID teacherId, UUID id, MultipartFile file) {
        Course course = findOwned(teacherId, id);
        course.setImageUrl(fileStorageService.storeImage(file, "courses"));
        return toDetail(course);
    }

    @Override
    @Transactional
    public void deleteCourse(UUID teacherId, UUID id) {
        findOwned(teacherId, id).setDeletedAt(Instant.now());
    }

    @Override
    @Transactional(readOnly = true)
    public CourseBriefResponse getCourseBrief(UUID id) {
        return courseMapper.toBriefResponse(courseRepository.findById(id).orElseThrow(this::courseNotFound));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseBriefResponse> getCourseBriefs(Collection<UUID> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return courseRepository.findAllById(ids).stream().map(courseMapper::toBriefResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseBriefResponse> searchCourseBriefs(UUID teacherId, String name, String code, PageRequestParams params) {
        CourseSearchRequest request = new CourseSearchRequest(code, name, null, null, null, null);
        return courseRepository.findAll(CourseSpecification.search(teacherId, null, request), pageable(params))
                .map(courseMapper::toBriefResponse);
    }

    private org.springframework.data.domain.Pageable pageable(PageRequestParams params) {
        return params.toPageable(SORTABLE_FIELDS, "createdAt");
    }

    // Tên thể loại và giảng viên lấy theo lô cho cả trang, tránh N+1.
    private Page<CourseSummaryResponse> toSummaryPage(Page<Course> page) {
        Set<UUID> categoryIds = page.stream().map(Course::getCategoryId).collect(Collectors.toSet());
        Set<UUID> teacherIds = page.stream().map(Course::getTeacherId).collect(Collectors.toSet());
        Map<UUID, String> categoryNames = categoryRepository.findAllById(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
        Map<UUID, String> teacherNames = new HashMap<>();
        userService.getUserBriefs(teacherIds).forEach(b -> teacherNames.put(b.id(), b.fullName()));
        return page.map(c -> courseMapper.toSummaryResponse(
                c, categoryNames.get(c.getCategoryId()), teacherNames.get(c.getTeacherId())));
    }

    private CourseDetailResponse toDetail(Course course) {
        CategoryRefResponse category = categoryRepository.findById(course.getCategoryId())
                .map(categoryMapper::toRefResponse).orElse(null);
        TeacherRefResponse teacher = userService.getUserBriefs(List.of(course.getTeacherId())).stream()
                .findFirst().map(this::toTeacherRef).orElse(null);
        List<LectureRefResponse> lectures = lectureRepository.findByCourseIdOrderByCreatedAtAscIdAsc(course.getId())
                .stream().map(lectureMapper::toRefResponse).toList();
        return courseMapper.toDetailResponse(course, category, teacher, lectures);
    }

    private TeacherRefResponse toTeacherRef(UserBriefResponse brief) {
        return new TeacherRefResponse(brief.id(), brief.fullName());
    }

    private Course findOwned(UUID teacherId, UUID id) {
        return courseRepository.findByIdAndTeacherId(id, teacherId).orElseThrow(this::courseNotFound);
    }

    private void requireCategory(UUID categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy thể loại.");
        }
    }

    private void defaultPrice(Course course) {
        if (course.getPrice() == null) {
            course.setPrice(BigDecimal.ZERO);
        }
    }

    // Mã dạng CO + 6 chữ số; trùng thì thử lại, unique constraint là chốt chặn cuối.
    private String generateCode() {
        for (int i = 0; i < CODE_GENERATION_ATTEMPTS; i++) {
            String code = "CO%06d".formatted(ThreadLocalRandom.current().nextInt(1_000_000));
            if (!courseRepository.existsByCode(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Không sinh được mã khóa học không trùng sau " + CODE_GENERATION_ATTEMPTS + " lần thử");
    }

    private BusinessException courseNotFound() {
        return new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy khóa học.");
    }
}
