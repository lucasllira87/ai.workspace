package com.aiworkspace.learning.application.dto;

import com.aiworkspace.learning.domain.model.QuizQuestion;

import java.util.List;
import java.util.UUID;

public record QuizQuestionDto(UUID id, String question, List<String> options,
                               int correctOptionIndex, String explanation) {
    public static QuizQuestionDto from(QuizQuestion q) {
        return new QuizQuestionDto(q.getId(), q.getQuestion(), q.getOptions(),
                q.getCorrectOptionIndex(), q.getExplanation());
    }
}
