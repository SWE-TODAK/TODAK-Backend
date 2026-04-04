package com.sogong.todak.transcription.entity;

import com.sogong.todak.recording.entity.Recording;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "transcriptions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Transcription {

    @Id
    @Column(name = "transcription_id", nullable = false)
    private UUID transcriptionId;

    @Column(name = "recording_id", nullable = false, unique = true)
    private UUID recordingId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recording_id", insertable = false, updatable = false)
    private Recording recording;

    @Column(name = "transcript_text", nullable = false, columnDefinition = "TEXT")
    private String transcriptText;

    @Column(name = "language", length = 20)
    private String language;

    @Column(name = "provider", length = 50)
    private String provider;

    @Column(name = "model", length = 100)
    private String model;

    @Column(name = "meta_json", columnDefinition = "TEXT")
    private String metaJson;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public static Transcription create(
            UUID recordingId,
            String transcriptText,
            String language,
            String provider,
            String model,
            String metaJson
    ) {
        OffsetDateTime now = OffsetDateTime.now();
        return Transcription.builder()
                .transcriptionId(UUID.randomUUID())
                .recordingId(recordingId)
                .transcriptText(transcriptText)
                .language(language)
                .provider(provider)
                .model(model)
                .metaJson(metaJson)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}