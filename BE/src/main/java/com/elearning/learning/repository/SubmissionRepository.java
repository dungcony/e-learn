package com.elearning.learning.repository;

import com.elearning.learning.entity.Submission;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubmissionRepository extends JpaRepository<Submission, UUID> {

    Optional<Submission> findByStudentIdAndExerciseId(UUID studentId, UUID exerciseId);

    /**
     * Như {@link #findByStudentIdAndExerciseId} nhưng khóa bi quan dòng bài làm ({@code FOR UPDATE}), để hai request
     * nộp cùng một bài không cùng thành công. Giao dịch gọi hàm này phải ngắn.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Submission> findWithLockByStudentIdAndExerciseId(UUID studentId, UUID exerciseId);

    List<Submission> findByStudentIdAndExerciseIdIn(UUID studentId, Collection<UUID> exerciseIds);
}
