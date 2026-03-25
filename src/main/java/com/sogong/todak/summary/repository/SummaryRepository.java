package com.sogong.todak.summary.repository;

import com.sogong.todak.summary.entity.Summary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SummaryRepository extends JpaRepository<Summary, UUID> {

    Optional<Summary> findByRecordingId(UUID recordingId);
    List<Summary> findByRecordingIdIn(Collection<UUID> recordingIds);

    boolean existsByRecordingId(UUID recordingId);
}