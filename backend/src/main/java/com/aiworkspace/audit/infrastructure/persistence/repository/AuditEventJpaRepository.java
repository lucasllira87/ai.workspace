package com.aiworkspace.audit.infrastructure.persistence.repository;

import com.aiworkspace.audit.infrastructure.persistence.entity.AuditEventId;
import com.aiworkspace.audit.infrastructure.persistence.entity.AuditEventJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AuditEventJpaRepository extends JpaRepository<AuditEventJpaEntity, AuditEventId> {

    // For paginated audit trail endpoint
    Page<AuditEventJpaEntity> findByUserIdOrderByOccurredAtDesc(UUID userId, Pageable pageable);

    // For recent activity in dashboard — Pageable applies LIMIT without a count query
    @Query("SELECT e FROM AuditEventJpaEntity e WHERE e.userId = :userId ORDER BY e.occurredAt DESC")
    List<AuditEventJpaEntity> findRecentByUserId(@Param("userId") UUID userId, Pageable pageable);
}
