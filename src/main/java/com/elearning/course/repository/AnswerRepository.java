package com.elearning.course.repository;

import com.elearning.course.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface AnswerRepository extends JpaRepository<Answer, UUID> {

    List<Answer> findByQuestionIdInOrderByPositionAsc(Collection<UUID> questionIds);
}
