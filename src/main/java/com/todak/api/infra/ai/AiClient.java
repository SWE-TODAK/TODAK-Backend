package com.todak.api.infra.ai;

import com.todak.api.infra.ai.dto.AiSttResponseDto;
import com.todak.api.infra.ai.dto.AiSummaryResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
import java.util.HashMap;
import org.springframework.core.io.Resource;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiClient {

    private final RestTemplate restTemplate;

    @Value("${ai.server.base-url}")
    private String aiBaseUrl;

    @Value("${ai.server.internal-key}")
    private String internalKey;
    /**
     * Spring → AI 서버에 Whisper STT 요청
     */
    public AiSttResponseDto requestStt(
            Long recordingId,
            Long consultationId,
            MultipartFile file
    ) {
        try {
            // -----------------------------
            // multipart/form-data 구성
            // -----------------------------
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("recordingId", recordingId.toString());
            body.add("consultationId", consultationId.toString());
            body.add("language", "ko"); // 기본 언어

            // 파일 파트
            // filename 포함된 Resource로 교체
            InputStreamResource fileResource = new InputStreamResource(file.getInputStream()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename(); // 파일명 전달이 핵심!!
                }
            };

            HttpHeaders fileHeaders = new HttpHeaders();
            fileHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            HttpEntity<Resource> fileEntity = new HttpEntity<>(fileResource, fileHeaders);

// multipart body에 추가
            body.add("file", fileEntity);


            // -----------------------------
            // 최상위 Header
            // -----------------------------
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("X-Internal-Key", internalKey);

            HttpEntity<MultiValueMap<String, Object>> requestEntity =
                    new HttpEntity<>(body, headers);

            // -----------------------------
            // 요청 URL
            // -----------------------------
            String url = aiBaseUrl + "/internal/transcriptions";

            log.info("[AI] STT Request → recordingId={}, url={}", recordingId, url);

            // -----------------------------
            // 요청 실행
            // -----------------------------
            ResponseEntity<AiSttResponseDto> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.POST,
                            requestEntity,
                            AiSttResponseDto.class
                    );

            if (response.getBody() == null) {
                throw new IllegalStateException("AI 서버 응답이 비어 있습니다.");
            }

            log.info("[AI] STT Success → recordingId={}, duration={}",
                    recordingId,
                    response.getBody().getData().getDuration()
            );

            return response.getBody();

        } catch (Exception e) {
            log.error("[AI] STT 요청 실패 recordingId={} : {}", recordingId, e.getMessage());
            throw new RuntimeException("AI 서버 STT 호출 실패", e);
        }
    }

    /**
     * Spring → AI 서버에 요약(Summarize) 요청
     * 내부 API: POST {aiBaseUrl}/internal/summarizes
     */
    public AiSummaryResponseDto requestSummary(
            Long consultationId,
            Long recordingId,
            String transcript
    ) {
        try {
            // -----------------------------
            // 요청 URL
            // -----------------------------
            String url = aiBaseUrl + "/internal/summarizes";

            log.info("[AI] Summary Request → consultationId={}, recordingId={}, url={}",
                    consultationId, recordingId, url);

            // -----------------------------
            // JSON Body 구성
            // -----------------------------
            Map<String, Object> body = new HashMap<>();
            body.put("consultationId", consultationId.toString());
            body.put("recordingId", recordingId.toString());
            body.put("transcript", transcript);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Internal-Key", internalKey);

            HttpEntity<Map<String, Object>> requestEntity =
                    new HttpEntity<>(body, headers);

            // -----------------------------
            // 요청 실행
            // -----------------------------
            ResponseEntity<AiSummaryResponseDto> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.POST,
                            requestEntity,
                            AiSummaryResponseDto.class
                    );

            if (response.getBody() == null) {
                throw new IllegalStateException("AI 서버 Summary 응답이 비어 있습니다.");
            }

            log.info("[AI] Summary Success → consultationId={}, recordingId={}",
                    consultationId, recordingId
            );

            return response.getBody();

        } catch (Exception e) {
            log.error("[AI] Summary 요청 실패 consultationId={}, recordingId={} : {}",
                    consultationId, recordingId, e.getMessage());
            throw new RuntimeException("AI 서버 Summary 호출 실패", e);
        }
    }
}
