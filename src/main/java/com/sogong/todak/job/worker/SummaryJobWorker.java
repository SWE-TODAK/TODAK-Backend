package com.sogong.todak.job.worker;

import com.sogong.todak.job.entity.Job;
import com.sogong.todak.job.entity.JobStatus;
import com.sogong.todak.job.entity.JobType;
import com.sogong.todak.job.repository.JobRepository;
import com.sogong.todak.job.service.SummaryProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

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

        for (Job job : jobs) {
            summaryProcessor.process(job.getJobId());
        }
    }
}