package com.sogong.todak.recording.repository;

import com.sogong.todak.recording.entity.Recording;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RecordingRepository extends JpaRepository<Recording, UUID> {
    Optional<Recording> findByRecordingIdAndUserId(UUID recordingId, UUID userId);
}