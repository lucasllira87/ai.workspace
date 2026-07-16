package com.aiworkspace.learning.infrastructure.persistence.adapter;

import com.aiworkspace.learning.application.port.out.CourseRepository;
import com.aiworkspace.learning.domain.model.Course;
import com.aiworkspace.learning.domain.model.CourseLevel;
import com.aiworkspace.learning.domain.model.CourseStatus;
import com.aiworkspace.learning.domain.model.Lesson;
import com.aiworkspace.learning.domain.model.LessonType;
import com.aiworkspace.learning.domain.model.QuizQuestion;
import com.aiworkspace.learning.infrastructure.persistence.entity.CourseJpaEntity;
import com.aiworkspace.learning.infrastructure.persistence.entity.LessonJpaEntity;
import com.aiworkspace.learning.infrastructure.persistence.entity.QuizQuestionJpaEntity;
import com.aiworkspace.learning.infrastructure.persistence.repository.CourseJpaRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class CourseRepositoryAdapter implements CourseRepository {

    private final CourseJpaRepository jpaRepository;
    private final ObjectMapper objectMapper;

    public CourseRepositoryAdapter(CourseJpaRepository jpaRepository, ObjectMapper objectMapper) {
        this.jpaRepository = jpaRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public Course save(Course course) {
        CourseJpaEntity entity = toEntity(course);
        jpaRepository.save(entity);
        return course;
    }

    @Override
    public Optional<Course> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Course> findAllPublished() {
        return jpaRepository.findAllPublished().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Course> findAllByOwnerId(UUID ownerId) {
        return jpaRepository.findAllByOwnerIdOrderByCreatedAtDesc(ownerId).stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    private CourseJpaEntity toEntity(Course course) {
        CourseJpaEntity entity = new CourseJpaEntity();
        entity.setId(course.getId());
        entity.setOwnerId(course.getOwnerId());
        entity.setTitle(course.getTitle());
        entity.setDescription(course.getDescription());
        entity.setLevel(course.getLevel().name());
        entity.setCategory(course.getCategory());
        entity.setStatus(course.getStatus().name());
        entity.setCreatedAt(course.getCreatedAt());
        entity.setUpdatedAt(course.getUpdatedAt());

        List<LessonJpaEntity> lessonEntities = new ArrayList<>();
        for (Lesson lesson : course.getLessons()) {
            LessonJpaEntity lessonEntity = toLessonEntity(lesson);
            lessonEntity.setCourse(entity);

            List<QuizQuestionJpaEntity> quizEntities = new ArrayList<>();
            for (QuizQuestion q : lesson.getQuizQuestions()) {
                QuizQuestionJpaEntity quizEntity = toQuizEntity(q);
                quizEntity.setLesson(lessonEntity);
                quizEntities.add(quizEntity);
            }
            lessonEntity.setQuizQuestions(quizEntities);
            lessonEntities.add(lessonEntity);
        }
        entity.setLessons(lessonEntities);
        return entity;
    }

    private LessonJpaEntity toLessonEntity(Lesson lesson) {
        LessonJpaEntity entity = new LessonJpaEntity();
        entity.setId(lesson.getId());
        entity.setTitle(lesson.getTitle());
        entity.setContent(lesson.getContent());
        entity.setVideoUrl(lesson.getVideoUrl());
        entity.setType(lesson.getType().name());
        entity.setLessonOrder(lesson.getLessonOrder());
        entity.setEstimatedMinutes(lesson.getEstimatedMinutes());
        return entity;
    }

    private QuizQuestionJpaEntity toQuizEntity(QuizQuestion q) {
        QuizQuestionJpaEntity entity = new QuizQuestionJpaEntity();
        entity.setId(q.getId());
        entity.setQuestion(q.getQuestion());
        entity.setOptions(serializeOptions(q.getOptions()));
        entity.setCorrectOptionIndex(q.getCorrectOptionIndex());
        entity.setExplanation(q.getExplanation());
        return entity;
    }

    private Course toDomain(CourseJpaEntity entity) {
        List<Lesson> lessons = entity.getLessons().stream()
                .map(l -> toDomainLesson(l, entity.getId()))
                .toList();

        return Course.reconstitute(
                entity.getId(),
                entity.getOwnerId(),
                entity.getTitle(),
                entity.getDescription(),
                CourseLevel.valueOf(entity.getLevel()),
                entity.getCategory(),
                CourseStatus.valueOf(entity.getStatus()),
                lessons,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private Lesson toDomainLesson(LessonJpaEntity entity, UUID courseId) {
        List<QuizQuestion> questions = entity.getQuizQuestions().stream()
                .map(this::toDomainQuiz)
                .toList();

        return new Lesson(
                entity.getId(),
                courseId,
                entity.getTitle(),
                entity.getContent(),
                entity.getVideoUrl(),
                LessonType.valueOf(entity.getType()),
                entity.getLessonOrder(),
                entity.getEstimatedMinutes(),
                questions
        );
    }

    private QuizQuestion toDomainQuiz(QuizQuestionJpaEntity entity) {
        return new QuizQuestion(
                entity.getId(),
                entity.getQuestion(),
                deserializeOptions(entity.getOptions()),
                entity.getCorrectOptionIndex(),
                entity.getExplanation()
        );
    }

    private String serializeOptions(List<String> options) {
        try {
            return objectMapper.writeValueAsString(options);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize quiz options", e);
        }
    }

    private List<String> deserializeOptions(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize quiz options", e);
        }
    }
}
