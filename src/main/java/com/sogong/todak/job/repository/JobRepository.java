package com.sogong.todak.job.repository;

import com.sogong.todak.job.entity.Job;
import com.sogong.todak.job.entity.JobStatus;
import com.sogong.todak.job.entity.JobType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobRepository extends JpaRepository<Job, UUID> {
    Optional<Job> findByRecordingIdAndJobType(UUID recordingId, JobType jobType);

    List<Job> findTop10ByJobTypeAndStatusOrderByCreatedAtAsc(JobType jobType, JobStatus status);
}