package com.sogong.todak.transcription.repository;

import com.sogong.todak.transcription.entity.Transcription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TranscriptionRepository extends JpaRepository<Transcription, UUID> {
    Optional<Transcription> findByRecordingId(UUID recordingId);
}