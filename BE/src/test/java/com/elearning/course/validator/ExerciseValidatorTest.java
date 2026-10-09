package com.elearning.course.validator;

import com.elearning.common.exception.BusinessException;
import com.elearning.course.dto.request.ExerciseAnswerRequest;
import com.elearning.course.dto.request.ExerciseQuestionRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExerciseValidatorTest {

    private final ExerciseValidator validator = new ExerciseValidator();

    @Test
    void validateSingleCorrectAnswer_exactlyOneCorrectPerQuestionPasses() {
        assertThatCode(() -> validator.validateSingleCorrectAnswer(List.of(question(true, false, false, false),
                question(false, false, true, false)))).doesNotThrowAnyException();
    }

    @Test
    void validateSingleCorrectAnswer_noCorrectAnswerFails() {
        assertInvalid(List.of(question(false, false, false, false)));
    }

    @Test
    void validateSingleCorrectAnswer_twoCorrectAnswersFails() {
        assertInvalid(List.of(question(true, true, false, false)));
    }

    @Test
    void validateSingleCorrectAnswer_oneBadQuestionAmongGoodOnesFails() {
        assertInvalid(List.of(question(true, false, false, false), question(true, true, true, true)));
    }

    private void assertInvalid(List<ExerciseQuestionRequest> questions) {
        assertThatThrownBy(() -> validator.validateSingleCorrectAnswer(questions))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo("EXERCISE_ANSWER_INVALID"));
    }

    private ExerciseQuestionRequest question(boolean... correct) {
        return new ExerciseQuestionRequest("Câu hỏi?", java.util.stream.IntStream.range(0, correct.length)
                .mapToObj(i -> new ExerciseAnswerRequest("Đáp án " + i, correct[i])).toList());
    }
}
