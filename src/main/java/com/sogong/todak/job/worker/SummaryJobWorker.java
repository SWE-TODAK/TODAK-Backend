package com.sogong.todak.job.worker;

import com.sogong.todak.ai.AiClient;
import com.sogong.todak.ai.dto.AiSummaryResponse;
import com.sogong.todak.ai.dto.SummaryRequest;
import com.sogong.todak.job.entity.Job;
import com.sogong.todak.job.entity.JobStatus;
import com.sogong.todak.job.entity.JobType;
import com.sogong.todak.job.repository.JobRepository;
import com.sogong.todak.job.service.SummaryProcessor;
import com.sogong.todak.recording.repository.RecordingRepository; // Recording 조회를 위해 필요
import com.sogong.todak.summary.entity.Summary;
import com.sogong.todak.summary.repository.SummaryRepository;
import com.sogong.todak.transcription.repository.TranscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class SummaryJobWorker {

    private final JobRepository jobRepository;
    private final SummaryProcessor summaryProcessor;

    @Scheduled(fixedDelay = 5000)
    public void processSummaryJobs() {
        List<Job> jobs = jobRepository.findTop10ByJobTypeAndStatusOrderByCreatedAtAsc(
                JobType.SUMMARY, JobStatus.QUEUED);

        if (jobs.isEmpty()) return;

        for (Job job : jobs) {
            if (summaryProcessor.startJob(job.getJobId())) {
                summaryProcessor.process(job.getJobId());
            }
        }
    }
}