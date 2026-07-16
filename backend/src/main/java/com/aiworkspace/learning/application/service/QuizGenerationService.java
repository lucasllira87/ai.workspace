package com.aiworkspace.learning.application.service;

import com.aiworkspace.learning.application.command.GenerateQuizCommand;
import com.aiworkspace.learning.application.dto.CourseDto;
import com.aiworkspace.learning.application.port.in.GenerateQuizUseCase;
import com.aiworkspace.learning.application.port.out.AIQuizPort;
import com.aiworkspace.learning.application.port.out.CourseRepository;
import com.aiworkspace.learning.domain.exception.CourseNotFoundException;
import com.aiworkspace.learning.domain.model.Course;
import com.aiworkspace.learning.domain.model.Lesson;
import com.aiworkspace.learning.domain.model.QuizQuestion;
import com.aiworkspace.shared.exception.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QuizGenerationService implements GenerateQuizUseCase {

    private static final int MAX_QUESTIONS = 20;

    private final CourseRepository courseRepository;
    private final AIQuizPort aiQuizPort;

    public QuizGenerationService(CourseRepository courseRepository, AIQuizPort aiQuizPort) {
        this.courseRepository = courseRepository;
        this.aiQuizPort = aiQuizPort;
    }

    @Override
    @Transactional
    public CourseDto generateQuiz(GenerateQuizCommand command) {
        if (command.questionCount() < 1 || command.questionCount() > MAX_QUESTIONS) {
            throw new DomainException("Question count must be between 1 and " + MAX_QUESTIONS);
        }

        Course course = courseRepository.findById(command.courseId())
                .orElseThrow(() -> new CourseNotFoundException(command.courseId()));

        if (!course.getOwnerId().equals(command.ownerId())) {
            throw new CourseNotFoundException(command.courseId());
        }

        Lesson lesson = course.getLessonById(command.lessonId());

        if (lesson.getContent() == null || lesson.getContent().isBlank()) {
            throw new DomainException("Lesson must have content before generating a quiz");
        }

        List<QuizQuestion> questions = aiQuizPort.generateQuestions(
                lesson.getContent(), lesson.getTitle(), command.questionCount());

        course.setLessonQuizQuestions(command.lessonId(), questions);
        Course saved = courseRepository.save(course);
        return CourseDto.from(saved);
    }
}
