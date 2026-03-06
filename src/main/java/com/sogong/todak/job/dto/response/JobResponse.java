package com.sogong.todak.job.dto.response;

import com.sogong.todak.job.entity.Job;
import com.sogong.todak.job.entity.JobStatus;
import com.sogong.todak.job.entity.JobType;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class JobResponse {
    private UUID jobId;
    private UUID recordingId;
    private JobType jobType;
    private JobStatus status;
    private Integer attemptCount;
    private String errorMessage;

    public static JobResponse from(Job job) {
        return JobResponse.builder()
                .jobId(job.getJobId())
                .recordingId(job.getRecordingId())
                .jobType(job.getJobType())
                .status(job.getStatus())
                .attemptCount(job.getAttemptCount())
                .errorMessage(job.getErrorMessage())
                .build();
    }
}