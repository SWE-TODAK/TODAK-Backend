package com.sogong.todak.job.controller;

import com.sogong.todak.job.dto.response.JobResponse;
import com.sogong.todak.job.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/jobs")
public class JobController {

    private final JobService jobService;

    @GetMapping("/{jobId}")
    public ResponseEntity<JobResponse> getJob(@PathVariable UUID jobId) {
        return ResponseEntity.ok(jobService.getJob(jobId));
    }
}