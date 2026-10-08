package com.elearning.course.validator;

import com.elearning.common.exception.BusinessException;
import com.elearning.common.exception.ErrorCode;
import com.elearning.course.dto.request.ExerciseAnswerRequest;
import com.elearning.course.dto.request.ExerciseQuestionRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExerciseValidator {

    /**
     * Mỗi câu hỏi phải có đúng một đáp án đúng. Số đáp án (4) đã được Bean Validation kiểm tra ở DTO.
     *
     * @throws BusinessException {@code EXERCISE_ANSWER_INVALID} nếu có câu có số đáp án đúng khác 1
     */
    public void validateSingleCorrectAnswer(List<ExerciseQuestionRequest> questions) {
        for (ExerciseQuestionRequest question : questions) {
            long correct = question.answers().stream().filter(ExerciseAnswerRequest::isCorrect).count();
            if (correct != 1) {
                throw new BusinessException(ErrorCode.EXERCISE_ANSWER_INVALID);
            }
        }
    }
}
