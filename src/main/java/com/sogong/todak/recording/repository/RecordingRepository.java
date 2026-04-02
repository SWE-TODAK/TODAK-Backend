package com.sogong.todak.recording.repository;

import com.sogong.todak.recording.entity.Recording;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecordingRepository extends JpaRepository<Recording, UUID> {

    Optional<Recording> findByRecordingIdAndUser_UserId(UUID recordingId, UUID userId);

    @Query("SELECT r FROM Recording r LEFT JOIN FETCH r.summary WHERE r.user.userId = :userId ORDER BY r.createdAt DESC")
    List<Recording> findAllWithSummaryByUserId(@Param("userId") UUID userId);

    List<Recording> findTop5ByUser_UserIdOrderByCreatedAtDesc(UUID userId);
    @EntityGraph(attributePaths = {"summary"})
    List<Recording> findTop4ByUser_UserIdOrderByCreatedAtDesc(UUID userId);
}