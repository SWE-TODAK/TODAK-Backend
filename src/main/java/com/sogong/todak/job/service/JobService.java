package com.sogong.todak.job.service;

import com.sogong.todak.job.dto.response.JobResponse;
import com.sogong.todak.job.entity.Job;
import com.sogong.todak.job.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;

    @Transactional(readOnly = true)
    public JobResponse getJob(UUID jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));
        return JobResponse.from(job);
    }
}