package com.sogong.todak.recording.repository;

import com.sogong.todak.recording.entity.Recording;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecordingRepository extends JpaRepository<Recording, UUID> {

    // 메모 수정/삭제 등 가벼운 작업용
    Optional<Recording> findByRecordingIdAndUser_UserId(UUID recordingId, UUID userId);

    // 상세 조회용 (Summary, Transcription을 쿼리 1번으로 가져와서 N+1 방지)
    @EntityGraph(attributePaths = {"summary", "transcription"})
    Optional<Recording> findWithDetailsByRecordingIdAndUser_UserId(UUID recordingId, UUID userId);

    @Query("SELECT r FROM Recording r LEFT JOIN FETCH r.summary WHERE r.user.userId = :userId ORDER BY r.createdAt DESC")
    List<Recording> findAllWithSummaryByUserId(@Param("userId") UUID userId);

    @EntityGraph(attributePaths = {"summary"})
    List<Recording> findTop4ByUser_UserIdOrderByCreatedAtDesc(UUID userId);

    @EntityGraph(attributePaths = {"summary"})
    List<Recording> findByUser_UserIdAndCreatedAtBetween(UUID userId, OffsetDateTime start, OffsetDateTime end);

    List<Recording> findByUser_UseerIdAndCreatedAtBetween(OffsetDateTime createdAtAfter, OffsetDateTime createdAtBefore);
}