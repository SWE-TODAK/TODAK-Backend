package com.sogong.todak.recording.entity;

import com.sogong.todak.user.entity.User;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

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

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "memo", columnDefinition = "TEXT")
    private String memo;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "hospital_name", length = 255)
    private String hospitalName;

    @Column(name = "disease_name", length = 255)
    private String diseaseName;

    @Column(name = "doctor_name", length = 100)
    private String doctorName;

    @Column(name = "department_name", length = 100)
    private String departmentName;

    @Column(name = "consulted_at")
    private OffsetDateTime consultedAt;

    @Column(name = "audio_url")
    private String audioUrl;

    @OneToOne(mappedBy = "recording", cascade = CascadeType.ALL)
    private com.sogong.todak.transcription.entity.Transcription transcription;

    @OneToOne(mappedBy = "recording", cascade = CascadeType.ALL)
    private com.sogong.todak.summary.entity.Summary summary;

    public static Recording create(User user) {
        var now = OffsetDateTime.now();
        return Recording.builder()
                .recordingId(UUID.randomUUID())
                .user(user)
                .status(RecordingStatus.CREATED)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void updateMedicalMetadata(
            String hospitalName,
            String diseaseName,
            String doctorName,
            String departmentName,
            OffsetDateTime consultedAt,
            String title
    ) {
        this.hospitalName = hospitalName;
        this.diseaseName = diseaseName;
        this.doctorName = doctorName;
        this.departmentName = departmentName;
        this.consultedAt = consultedAt;
        this.title = title;
        this.updatedAt = OffsetDateTime.now();
    }

    public void markUploaded(String storageKey, String mimeType, Integer durationMs, Integer sampleRate) {
        this.storageKey = storageKey;
        this.mimeType = mimeType;
        this.durationMs = durationMs;
        this.sampleRate = sampleRate;
        this.status = RecordingStatus.UPLOADED;
        this.updatedAt = OffsetDateTime.now();
    }

    public void markProcessing() {
        this.status = RecordingStatus.PROCESSING;
        this.updatedAt = OffsetDateTime.now();
    }
    public void updateMemo(String memo) {
        this.memo = memo;
        this.updatedAt = OffsetDateTime.now();
    }

    public void deleteMemo() {
        this.memo = null;
        this.updatedAt = OffsetDateTime.now();
    }

    public void markDone() {
        this.status = RecordingStatus.DONE;
        this.updatedAt = OffsetDateTime.now();
    }

    public void markFailed() {
        this.status = RecordingStatus.FAILED;
        this.updatedAt = OffsetDateTime.now();
    }
}