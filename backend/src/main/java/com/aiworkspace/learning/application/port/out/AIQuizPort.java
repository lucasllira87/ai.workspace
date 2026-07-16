package com.aiworkspace.learning.application.port.out;

import com.aiworkspace.learning.domain.model.QuizQuestion;

import java.util.List;

public interface AIQuizPort {
    List<QuizQuestion> generateQuestions(String lessonContent, String lessonTitle, int count);
}
