package com.sogong.todak.job.service;

import com.sogong.todak.ai.AiClient;
import com.sogong.todak.ai.dto.AiSummaryResponse;
import com.sogong.todak.ai.dto.SummaryRequest;
import com.sogong.todak.job.entity.Job;
import com.sogong.todak.job.entity.JobStatus;
import com.sogong.todak.job.repository.JobRepository;
import com.sogong.todak.summary.entity.Summary;
import com.sogong.todak.summary.repository.SummaryRepository;
import com.sogong.todak.transcription.entity.Transcription;
import com.sogong.todak.transcription.repository.TranscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SummaryProcessor {

    private final JobService jobService;
    private final JobRepository jobRepository;
    private final TranscriptionRepository transcriptionRepository;
    private final AiClient aiClient;

    public void process(UUID jobId) {
        // 1. 작업 선점 (중복 실행 방지)
        if (!jobService.claimJob(jobId)) {
            return;
        }

        try {
            // 2. 필요한 데이터 조회
            Job job = jobRepository.findById(jobId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Job입니다: " + jobId));

            UUID recordingId = job.getRecordingId();

            // 요약을 하려면 먼저 완료된 STT 결과(Transcription)가 있어야 합니다.
            Transcription transcription = transcriptionRepository.findByRecordingId(recordingId)
                    .orElseThrow(() -> new IllegalStateException("요약할 STT 결과가 존재하지 않습니다. RecordingId: " + recordingId));

            // [검증] 텍스트가 너무 짧으면 AI가 요약하기 어려우므로 미리 체크 (선택 사항)
            if (transcription.getTranscriptText() == null || transcription.getTranscriptText().length() < 20) {
                throw new IllegalArgumentException("요약하기에 텍스트 내용이 너무 부족합니다.");
            }

            log.info(">>>> [Summary] AI 서버 요약 요청 시작. RecordingId: {}", recordingId);

            // 3. AI 서버에 요약 요청 (네트워크 I/O)
            SummaryRequest request = new SummaryRequest(recordingId, transcription.getTranscriptText());
            AiSummaryResponse response = aiClient.requestSummary(request);

            // 4. [상세 응답 검증 및 Throw]
            if (response == null || response.data() == null) {
                throw new RuntimeException("AI 요약 응답이 비어있거나 올바르지 않습니다.");
            }

            if (response.data().intro() == null || response.data().content() == null) {
                throw new RuntimeException("AI 요약 결과(intro/content) 중 누락된 내용이 있습니다.");
            }

            log.info(">>>> [Summary] AI 서버 응답 수신 완료.");

            // 5. 결과 저장 및 Job 완료 처리 (JobService 위임)
            jobService.completeSummaryJob(jobId, recordingId, response);

        } catch (Exception e) {
            // 위에서 발생한 모든 예외(데이터 없음, API 에러, 검증 실패 등)를 잡아서 실패 처리
            log.error(">>>> [Summary] 처리 중 예외 발생 - JobID: {}", jobId, e);
            jobService.failJob(jobId, e);
        }
    }
}