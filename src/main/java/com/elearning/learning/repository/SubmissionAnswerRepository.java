package com.elearning.learning.repository;

import com.elearning.learning.entity.SubmissionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SubmissionAnswerRepository extends JpaRepository<SubmissionAnswer, UUID> {

    List<SubmissionAnswer> findBySubmissionId(UUID submissionId);
}
