package com.aiworkspace.learning.application.port.out;

import com.aiworkspace.learning.domain.model.Course;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourseRepository {
    Course save(Course course);
    Optional<Course> findById(UUID courseId);
    List<Course> findAllPublished();
    List<Course> findAllByOwnerId(UUID ownerId);
    boolean existsById(UUID courseId);
}
