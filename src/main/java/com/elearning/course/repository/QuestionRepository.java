package com.elearning.course.repository;

import com.elearning.course.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface QuestionRepository extends JpaRepository<Question, UUID> {

    List<Question> findByExerciseIdOrderByPositionAsc(UUID exerciseId);

    List<Question> findByExerciseIdInOrderByPositionAsc(Collection<UUID> exerciseIds);
}
