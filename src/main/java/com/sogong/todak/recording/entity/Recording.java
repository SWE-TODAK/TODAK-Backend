package com.sogong.todak.recording.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "recordings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Recording {

    @Id
    @Column(name = "recording_id", nullable = false)
    private UUID recordingId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private RecordingStatus status;

    @Column(name = "storage_key")
    private String storageKey;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "duration_ms")
    private Integer durationMs;

    @Column(name = "sample_rate")
    private Integer sampleRate;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public static Recording create(UUID userId) {
        var now = OffsetDateTime.now();
        return Recording.builder()
                .recordingId(UUID.randomUUID())
                .userId(userId)
                .status(RecordingStatus.CREATED)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void markUploaded(String storageKey, String mimeType, Integer durationMs, Integer sampleRate) {
        this.storageKey = storageKey;
        this.mimeType = mimeType;
        this.durationMs = durationMs;
        this.sampleRate = sampleRate;
        this.status = RecordingStatus.UPLOADED;
        this.updatedAt = OffsetDateTime.now();
    }
}