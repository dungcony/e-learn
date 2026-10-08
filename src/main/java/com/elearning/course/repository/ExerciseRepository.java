package com.elearning.course.repository;

import com.elearning.course.entity.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExerciseRepository extends JpaRepository<Exercise, UUID> {

    Optional<Exercise> findByIdAndLectureId(UUID id, UUID lectureId);

    List<Exercise> findByLectureIdOrderByCreatedAtAscIdAsc(UUID lectureId);
}
