package com.aiworkspace.learning.application.service;

import com.aiworkspace.learning.application.command.AddLessonCommand;
import com.aiworkspace.learning.application.command.CreateCourseCommand;
import com.aiworkspace.learning.application.command.GenerateLessonContentCommand;
import com.aiworkspace.learning.application.command.UpdateLessonCommand;
import com.aiworkspace.learning.application.dto.CourseDto;
import com.aiworkspace.learning.application.port.in.AddLessonUseCase;
import com.aiworkspace.learning.application.port.in.CreateCourseUseCase;
import com.aiworkspace.learning.application.port.in.GenerateLessonContentUseCase;
import com.aiworkspace.learning.application.port.in.GetCourseUseCase;
import com.aiworkspace.learning.application.port.in.ListCoursesUseCase;
import com.aiworkspace.learning.application.port.in.PublishCourseUseCase;
import com.aiworkspace.learning.application.port.in.RemoveLessonUseCase;
import com.aiworkspace.learning.application.port.in.UpdateLessonUseCase;
import com.aiworkspace.learning.application.port.out.AIContentPort;
import com.aiworkspace.learning.application.port.out.CourseRepository;
import com.aiworkspace.learning.domain.exception.CourseNotFoundException;
import com.aiworkspace.learning.domain.model.Course;
import com.aiworkspace.learning.domain.model.Lesson;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CourseService implements CreateCourseUseCase, PublishCourseUseCase,
        AddLessonUseCase, UpdateLessonUseCase, RemoveLessonUseCase,
        ListCoursesUseCase, GetCourseUseCase, GenerateLessonContentUseCase {

    private final CourseRepository courseRepository;
    private final AIContentPort aiContentPort;
    private final ApplicationEventPublisher eventPublisher;
    private final Counter courseCreatedCounter;
    private final Counter coursePublishedCounter;
    private final Timer aiContentTimer;

    public CourseService(CourseRepository courseRepository,
                          AIContentPort aiContentPort,
                          ApplicationEventPublisher eventPublisher,
                          MeterRegistry meterRegistry) {
        this.courseRepository = courseRepository;
        this.aiContentPort = aiContentPort;
        this.eventPublisher = eventPublisher;
        this.courseCreatedCounter = Counter.builder("learning.courses.created")
                .description("Total courses created").register(meterRegistry);
        this.coursePublishedCounter = Counter.builder("learning.courses.published")
                .description("Total courses published").register(meterRegistry);
        this.aiContentTimer = Timer.builder("learning.ai.content.generation")
                .description("Time to generate lesson content via AI").register(meterRegistry);
    }

    @Override
    @Transactional
    public CourseDto create(CreateCourseCommand command) {
        UUID courseId = UUID.randomUUID();
        Course course = Course.create(courseId, command.ownerId(), command.title(),
                command.description(), command.level(), command.category());
        Course saved = courseRepository.save(course);
        course.getDomainEvents().forEach(eventPublisher::publishEvent);
        courseCreatedCounter.increment();
        return CourseDto.from(saved);
    }

    @Override
    @Transactional
    public void publish(UUID courseId, UUID ownerId) {
        Course course = loadCourseForOwner(courseId, ownerId);
        course.publish();
        courseRepository.save(course);
        course.getDomainEvents().forEach(eventPublisher::publishEvent);
        coursePublishedCounter.increment();
    }

    @Override
    @Transactional
    public CourseDto addLesson(AddLessonCommand command) {
        Course course = loadCourseForOwner(command.courseId(), command.ownerId());
        int nextOrder = course.getLessons().size() + 1;
        Lesson lesson = new Lesson(UUID.randomUUID(), command.courseId(),
                command.title(), command.content(), command.videoUrl(),
                command.type(), nextOrder, command.estimatedMinutes(), List.of());
        course.addLesson(lesson);
        Course saved = courseRepository.save(course);
        return CourseDto.from(saved);
    }

    @Override
    @Transactional
    public CourseDto updateLesson(UpdateLessonCommand command) {
        Course course = loadCourseForOwner(command.courseId(), command.ownerId());
        course.updateLesson(command.lessonId(), command.title(), command.content(),
                command.videoUrl(), command.estimatedMinutes());
        Course saved = courseRepository.save(course);
        return CourseDto.from(saved);
    }

    @Override
    @Transactional
    public void removeLesson(UUID courseId, UUID lessonId, UUID ownerId) {
        Course course = loadCourseForOwner(courseId, ownerId);
        course.removeLesson(lessonId);
        courseRepository.save(course);
    }

    @Override
    @Transactional
    public CourseDto generateContent(GenerateLessonContentCommand command) {
        Course course = loadCourseForOwner(command.courseId(), command.ownerId());
        Lesson lesson = course.getLessonById(command.lessonId());
        String content = aiContentTimer.record(() ->
                aiContentPort.generateLessonContent(lesson.getTitle(), command.topic(), course.getLevel()));
        course.updateLessonContent(command.lessonId(), content);
        Course saved = courseRepository.save(course);
        return CourseDto.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseDto> listPublished() {
        return courseRepository.findAllPublished().stream().map(CourseDto::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseDto> listOwned(UUID ownerId) {
        return courseRepository.findAllByOwnerId(ownerId).stream().map(CourseDto::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDto getById(UUID courseId) {
        return courseRepository.findById(courseId)
                .map(CourseDto::from)
                .orElseThrow(() -> new CourseNotFoundException(courseId));
    }

    private Course loadCourseForOwner(UUID courseId, UUID ownerId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));
        if (!course.getOwnerId().equals(ownerId)) {
            throw new CourseNotFoundException(courseId);
        }
        return course;
    }
}
