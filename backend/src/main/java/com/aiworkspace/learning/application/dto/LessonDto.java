package com.aiworkspace.learning.application.dto;

import com.aiworkspace.learning.domain.model.Lesson;
import com.aiworkspace.learning.domain.model.LessonType;

import java.util.List;
import java.util.UUID;

public record LessonDto(UUID id, UUID courseId, String title, String content, String videoUrl,
                         LessonType type, int lessonOrder, int estimatedMinutes,
                         List<QuizQuestionDto> quizQuestions) {
    public static LessonDto from(Lesson lesson) {
        return new LessonDto(
                lesson.getId(), lesson.getCourseId(), lesson.getTitle(),
                lesson.getContent(), lesson.getVideoUrl(), lesson.getType(),
                lesson.getLessonOrder(), lesson.getEstimatedMinutes(),
                lesson.getQuizQuestions().stream().map(QuizQuestionDto::from).toList());
    }
}
