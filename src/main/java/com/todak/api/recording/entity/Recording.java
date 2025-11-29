// src/main/java/com/todak/api/recording/entity/Recording.java
package com.todak.api.recording.entity;

import com.todak.api.hospital.entity.Hospital;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "recordings")
public class Recording {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recording_id")
    private Long recordingId;

    // 진료 연결
    @Column(name = "consultation_id")
    private Long consultationId;

    // 병원 FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    // 파일 경로 (S3 URL)
    @Column(name = "file_path", columnDefinition = "TEXT")
    private String filePath;

    // 녹음 길이 (초)
    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    // 파일 크기 (MB)
    @Column(name = "file_size_mb")
    private Double fileSizeMb;

    // STT 결과 (원문 전체)
    @Column(columnDefinition = "TEXT")
    private String transcript;

    // 녹음 상태
    @Enumerated(EnumType.STRING)
    private RecordingStatus status;

    // 병원 인증 시각
    @Column(name = "authorized_at")
    private OffsetDateTime authorizedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}
