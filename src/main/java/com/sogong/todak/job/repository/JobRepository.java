package com.sogong.todak.job.repository;

import com.sogong.todak.job.entity.Job;
import com.sogong.todak.job.entity.JobStatus;
import com.sogong.todak.job.entity.JobType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobRepository extends JpaRepository<Job, UUID> {

    Optional<Job> findByRecordingIdAndJobType(UUID recordingId, JobType jobType);

    List<Job> findTop10ByJobTypeAndStatusOrderByCreatedAtAsc(JobType jobType, JobStatus status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Job j
           set j.status = :runningStatus,
               j.attemptCount = j.attemptCount + 1,
               j.updatedAt = CURRENT_TIMESTAMP
         where j.jobId = :jobId
           and j.status = :queuedStatus
    """)
    int claimJob(@Param("jobId") UUID jobId,
                 @Param("queuedStatus") JobStatus queuedStatus,
                 @Param("runningStatus") JobStatus runningStatus);
    void deleteByRecordingId(UUID recordingId);
}