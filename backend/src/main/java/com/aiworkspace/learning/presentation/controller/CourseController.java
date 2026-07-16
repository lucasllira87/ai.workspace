package com.aiworkspace.learning.presentation.controller;

import com.aiworkspace.learning.application.command.AddLessonCommand;
import com.aiworkspace.learning.application.command.CreateCourseCommand;
import com.aiworkspace.learning.application.command.GenerateLessonContentCommand;
import com.aiworkspace.learning.application.command.GenerateQuizCommand;
import com.aiworkspace.learning.application.command.UpdateLessonCommand;
import com.aiworkspace.learning.application.port.in.AddLessonUseCase;
import com.aiworkspace.learning.application.port.in.CreateCourseUseCase;
import com.aiworkspace.learning.application.port.in.GenerateLessonContentUseCase;
import com.aiworkspace.learning.application.port.in.GenerateQuizUseCase;
import com.aiworkspace.learning.application.port.in.GetCourseUseCase;
import com.aiworkspace.learning.application.port.in.ListCoursesUseCase;
import com.aiworkspace.learning.application.port.in.PublishCourseUseCase;
import com.aiworkspace.learning.application.port.in.RemoveLessonUseCase;
import com.aiworkspace.learning.application.port.in.UpdateLessonUseCase;
import com.aiworkspace.learning.application.port.out.LearningUserContextPort;
import com.aiworkspace.learning.presentation.request.AddLessonRequest;
import com.aiworkspace.learning.presentation.request.CreateCourseRequest;
import com.aiworkspace.learning.presentation.request.GenerateLessonContentRequest;
import com.aiworkspace.learning.presentation.request.GenerateQuizRequest;
import com.aiworkspace.learning.presentation.request.UpdateLessonRequest;
import com.aiworkspace.learning.presentation.response.CourseResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/courses")
public class CourseController {

    private final CreateCourseUseCase createCourse;
    private final PublishCourseUseCase publishCourse;
    private final AddLessonUseCase addLesson;
    private final UpdateLessonUseCase updateLesson;
    private final RemoveLessonUseCase removeLesson;
    private final ListCoursesUseCase listCourses;
    private final GetCourseUseCase getCourse;
    private final GenerateLessonContentUseCase generateContent;
    private final GenerateQuizUseCase generateQuiz;
    private final LearningUserContextPort userContext;

    public CourseController(CreateCourseUseCase createCourse,
                             PublishCourseUseCase publishCourse,
                             AddLessonUseCase addLesson,
                             UpdateLessonUseCase updateLesson,
                             RemoveLessonUseCase removeLesson,
                             ListCoursesUseCase listCourses,
                             GetCourseUseCase getCourse,
                             GenerateLessonContentUseCase generateContent,
                             GenerateQuizUseCase generateQuiz,
                             LearningUserContextPort userContext) {
        this.createCourse = createCourse;
        this.publishCourse = publishCourse;
        this.addLesson = addLesson;
        this.updateLesson = updateLesson;
        this.removeLesson = removeLesson;
        this.listCourses = listCourses;
        this.getCourse = getCourse;
        this.generateContent = generateContent;
        this.generateQuiz = generateQuiz;
        this.userContext = userContext;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseResponse create(@Valid @RequestBody CreateCourseRequest request) {
        UUID ownerId = userContext.getCurrentUserId();
        var dto = createCourse.create(new CreateCourseCommand(
                ownerId, request.title(), request.description(), request.level(), request.category()));
        return CourseResponse.from(dto);
    }

    @GetMapping
    public List<CourseResponse> list(@RequestParam(required = false) String view) {
        UUID userId = userContext.getCurrentUserId();
        if ("owned".equals(view)) {
            return listCourses.listOwned(userId).stream().map(CourseResponse::from).toList();
        }
        return listCourses.listPublished().stream().map(CourseResponse::from).toList();
    }

    @GetMapping("/{courseId}")
    public CourseResponse getById(@PathVariable UUID courseId) {
        return CourseResponse.from(getCourse.getById(courseId));
    }

    @PostMapping("/{courseId}/publish")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void publish(@PathVariable UUID courseId) {
        UUID ownerId = userContext.getCurrentUserId();
        publishCourse.publish(courseId, ownerId);
    }

    @PostMapping("/{courseId}/lessons")
    @ResponseStatus(HttpStatus.CREATED)
    public CourseResponse addLesson(@PathVariable UUID courseId,
                                     @Valid @RequestBody AddLessonRequest request) {
        UUID ownerId = userContext.getCurrentUserId();
        var dto = addLesson.addLesson(new AddLessonCommand(
                courseId, ownerId, request.title(), request.content(),
                request.videoUrl(), request.type(), request.estimatedMinutes()));
        return CourseResponse.from(dto);
    }

    @PutMapping("/{courseId}/lessons/{lessonId}")
    public CourseResponse updateLesson(@PathVariable UUID courseId,
                                        @PathVariable UUID lessonId,
                                        @Valid @RequestBody UpdateLessonRequest request) {
        UUID ownerId = userContext.getCurrentUserId();
        var dto = updateLesson.updateLesson(new UpdateLessonCommand(
                courseId, lessonId, ownerId, request.title(), request.content(),
                request.videoUrl(), request.estimatedMinutes()));
        return CourseResponse.from(dto);
    }

    @DeleteMapping("/{courseId}/lessons/{lessonId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeLesson(@PathVariable UUID courseId, @PathVariable UUID lessonId) {
        UUID ownerId = userContext.getCurrentUserId();
        removeLesson.removeLesson(courseId, lessonId, ownerId);
    }

    @PostMapping("/{courseId}/lessons/{lessonId}/generate-content")
    public CourseResponse generateContent(@PathVariable UUID courseId,
                                           @PathVariable UUID lessonId,
                                           @Valid @RequestBody GenerateLessonContentRequest request) {
        UUID ownerId = userContext.getCurrentUserId();
        var dto = generateContent.generateContent(
                new GenerateLessonContentCommand(courseId, lessonId, ownerId, request.topic()));
        return CourseResponse.from(dto);
    }

    @PostMapping("/{courseId}/lessons/{lessonId}/generate-quiz")
    public CourseResponse generateQuiz(@PathVariable UUID courseId,
                                        @PathVariable UUID lessonId,
                                        @Valid @RequestBody GenerateQuizRequest request) {
        UUID ownerId = userContext.getCurrentUserId();
        var dto = generateQuiz.generateQuiz(
                new GenerateQuizCommand(courseId, lessonId, ownerId, request.questionCount()));
        return CourseResponse.from(dto);
    }
}
