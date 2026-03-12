package com.sogong.todak.job.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "jobs",
        uniqueConstraints = {
                @UniqueConstraint(name = "ux_jobs_recording_type", columnNames = {"recording_id", "job_type"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Job {

    @Id
    @Column(name = "job_id", nullable = false)
    private UUID jobId;

    @Column(name = "recording_id", nullable = false)
    private UUID recordingId;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false, length = 30)
    private JobType jobType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private JobStatus status;

    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public static Job create(UUID recordingId, JobType jobType) {
        OffsetDateTime now = OffsetDateTime.now();
        return Job.builder()
                .jobId(UUID.randomUUID())
                .recordingId(recordingId)
                .jobType(jobType)
                .status(JobStatus.QUEUED)
                .attemptCount(0)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void markRunning() {
        this.status = JobStatus.RUNNING;
        this.updatedAt = OffsetDateTime.now();
    }

    public void markSucceeded() {
        this.status = JobStatus.SUCCEEDED;
        this.updatedAt = OffsetDateTime.now();
    }

    public void markFailed(String errorMessage) {
        this.status = JobStatus.FAILED;
        this.errorMessage = errorMessage;
        this.updatedAt = OffsetDateTime.now();
    }
}