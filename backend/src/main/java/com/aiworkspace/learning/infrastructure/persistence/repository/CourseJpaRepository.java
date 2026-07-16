package com.aiworkspace.learning.infrastructure.persistence.repository;

import com.aiworkspace.learning.infrastructure.persistence.entity.CourseJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CourseJpaRepository extends JpaRepository<CourseJpaEntity, UUID> {

    @Query("SELECT c FROM CourseJpaEntity c WHERE c.status = 'PUBLISHED' ORDER BY c.createdAt DESC")
    List<CourseJpaEntity> findAllPublished();

    List<CourseJpaEntity> findAllByOwnerIdOrderByCreatedAtDesc(UUID ownerId);
}
