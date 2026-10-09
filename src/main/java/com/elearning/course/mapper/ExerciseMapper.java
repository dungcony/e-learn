package com.elearning.course.mapper;

import com.elearning.common.config.CommonMapperConfig;
import com.elearning.course.dto.response.AnswerResponse;
import com.elearning.course.dto.response.ExerciseDetailResponse;
import com.elearning.course.dto.response.ExerciseSummaryResponse;
import com.elearning.course.dto.response.LearningAnswerResponse;
import com.elearning.course.dto.response.LearningExerciseResponse;
import com.elearning.course.dto.response.LearningQuestionResponse;
import com.elearning.course.dto.response.QuestionResponse;
import com.elearning.course.entity.Answer;
import com.elearning.course.entity.Exercise;
import com.elearning.course.entity.Question;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mapper(config = CommonMapperConfig.class)
public interface ExerciseMapper {

    ExerciseSummaryResponse toSummaryResponse(Exercise exercise);

    @Mapping(target = "isCorrect", source = "correct")
    AnswerResponse toAnswerResponse(Answer answer);

    LearningAnswerResponse toLearningAnswerResponse(Answer answer);

    // Câu hỏi và đáp án nằm ở bảng riêng, không có quan hệ ORM, nên ghép bằng map theo id cha.
    default ExerciseDetailResponse toDetailResponse(Exercise exercise, List<Question> questions,
                                                    Map<UUID, List<Answer>> answersByQuestion) {
        List<QuestionResponse> questionResponses = questions.stream()
                .map(q -> new QuestionResponse(q.getId(), q.getContent(),
                        answersByQuestion.getOrDefault(q.getId(), List.of()).stream().map(this::toAnswerResponse).toList()))
                .toList();
        return new ExerciseDetailResponse(exercise.getId(), exercise.getLectureId(), exercise.getTitle(),
                exercise.getDescription(), questionResponses);
    }

    default LearningExerciseResponse toLearningResponse(Exercise exercise, List<Question> questions,
                                                        Map<UUID, List<Answer>> answersByQuestion) {
        List<LearningQuestionResponse> questionResponses = questions.stream()
                .map(q -> new LearningQuestionResponse(q.getId(), q.getContent(),
                        answersByQuestion.getOrDefault(q.getId(), List.of()).stream().map(this::toLearningAnswerResponse).toList()))
                .toList();
        return new LearningExerciseResponse(exercise.getId(), exercise.getTitle(), exercise.getDescription(), questionResponses);
    }
}
