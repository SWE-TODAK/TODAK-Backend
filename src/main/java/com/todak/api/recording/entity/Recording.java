package com.todak.api.recording.entity;

import com.todak.api.consultation.entity.Consultation;
import com.todak.api.hospital.entity.Hospital;
import com.todak.api.user.entity.User;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", referencedColumnName = "user_uuid", nullable = false)
    private User patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id", nullable = false)
    private Consultation consultation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @Column(name = "file_path", columnDefinition = "TEXT")
    private String filePath;

    @Column(name = "format", length = 10)
    private String format;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "file_size_mb")
    private Double fileSizeMb;

    @Column(columnDefinition = "TEXT")
    private String transcript;

    @Enumerated(EnumType.STRING)
    private RecordingStatus status;

    @Column(name = "authorized_at")
    private OffsetDateTime authorizedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}
