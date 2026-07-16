package com.aiworkspace.documents.infrastructure.persistence.repository;

import com.aiworkspace.documents.infrastructure.persistence.entity.DocumentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentJpaRepository extends JpaRepository<DocumentJpaEntity, UUID> {

    Optional<DocumentJpaEntity> findByIdAndOwnerId(UUID id, UUID ownerId);

    List<DocumentJpaEntity> findAllByOwnerIdOrderByCreatedAtDesc(UUID ownerId);

    boolean existsByIdAndOwnerId(UUID id, UUID ownerId);

    @Query("SELECT e FROM DocumentJpaEntity e WHERE e.status = :status AND e.createdAt < :cutoff")
    List<DocumentJpaEntity> findByStatusAndCreatedAtBefore(
            @Param("status") String status, @Param("cutoff") Instant cutoff);

    @Modifying
    @Query("UPDATE DocumentJpaEntity d SET d.storageReference = :path, d.updatedAt = CURRENT_TIMESTAMP WHERE d.id = :id")
    void updateStoragePath(@Param("id") UUID id, @Param("path") String path);

    @Modifying
    @Query("UPDATE DocumentJpaEntity d SET d.status = :status, d.errorMessage = :errorMessage, d.updatedAt = CURRENT_TIMESTAMP WHERE d.id = :id")
    void updateStatus(@Param("id") UUID id, @Param("status") String status,
                      @Param("errorMessage") String errorMessage);

    // Dashboard stats queries
    long countByOwnerId(UUID ownerId);

    long countByOwnerIdAndStatus(UUID ownerId, String status);

    @Query("SELECT COALESCE(SUM(d.sizeBytes), 0) FROM DocumentJpaEntity d WHERE d.ownerId = :ownerId")
    long sumSizeBytesByOwnerId(@Param("ownerId") UUID ownerId);
}
