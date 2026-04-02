package com.sogong.todak.job.repository;

import com.sogong.todak.job.entity.Job;
import com.sogong.todak.job.entity.JobType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface JobRepository extends JpaRepository<Job, UUID> {

    Optional<Job> findByRecordingIdAndJobType(UUID recordingId, JobType jobType);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Job j WHERE j.recordingId = :recordingId")
    void deleteByRecordingId(@Param("recordingId") UUID recordingId);
}