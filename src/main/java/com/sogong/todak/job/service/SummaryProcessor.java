package com.sogong.todak.job.service;

import com.sogong.todak.ai.AiClient;
import com.sogong.todak.ai.dto.AiSummaryResponse;
import com.sogong.todak.ai.dto.SummaryRequest;
import com.sogong.todak.job.entity.Job;
import com.sogong.todak.job.repository.JobRepository;
import com.sogong.todak.transcription.entity.Transcription;
import com.sogong.todak.transcription.repository.TranscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

            var transcription = transcriptionRepository.findByRecordingId(recordingId)
                    .orElseThrow(() -> new IllegalStateException("STT 결과가 없습니다."));

            String originalText = transcription.getTranscriptText();

            // [변경 포인트] 텍스트가 짧은 경우 (예: 20자 미만)
            if (originalText == null || originalText.trim().length() < 20) {
                log.info(">>>> [Summary] 텍스트가 짧아 Fallback 로직을 실행합니다. ID: {}", recordingId);

                String fallbackIntro = "짧은 대화"; // 또는 "내용 요약"
                String fallbackContent = originalText;

                jobService.completeSummaryWithText(jobId, recordingId, fallbackIntro, fallbackContent);
                return; // 여기서 끝냄 (AI 호출 안 함)
            }

            // 텍스트가 충분할 때만 AI 호출
            SummaryRequest request = new SummaryRequest(recordingId, originalText);
            AiSummaryResponse response = aiClient.requestSummary(request);

            if (response == null || response.data() == null) {
                throw new RuntimeException("AI 요약 응답이 비어있습니다.");
            }

            // 정상 저장
            jobService.completeSummaryWithText(jobId, recordingId,
                    response.data().intro(), response.data().content());

        } catch (Exception e) {
            // 위에서 발생한 모든 예외(데이터 없음, API 에러, 검증 실패 등)를 잡아서 실패 처리
            log.error(">>>> [Summary] 처리 중 예외 발생 - JobID: {}", jobId, e);
            jobService.failJob(jobId, e);
        }
    }
}