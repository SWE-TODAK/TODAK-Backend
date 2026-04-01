package com.sogong.todak.job.worker;

import com.sogong.todak.job.entity.Job;
import com.sogong.todak.job.entity.JobStatus;
import com.sogong.todak.job.entity.JobType;
import com.sogong.todak.job.repository.JobRepository;
import com.sogong.todak.job.service.SttProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SttJobWorker {

    private final JobRepository jobRepository;
    private final SttProcessor sttProcessor;

    @Scheduled(fixedDelay = 5000)
    public void processQueuedSttJobs() {
        List<Job> jobs = jobRepository.findTop10ByJobTypeAndStatusOrderByCreatedAtAsc(
                JobType.STT, JobStatus.QUEUED);

        for (Job job : jobs) {
            // Processor 내부에서 선점(claim)부터 처리까지 한 번에 수행
            sttProcessor.process(job.getJobId());
        }
    }
}