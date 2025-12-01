package com.todak.api.recording.controller;

import com.todak.api.recording.dto.response.RecordingDetailResponseDto;
import com.todak.api.recording.entity.RecordingStatus;
import com.todak.api.recording.service.RecordingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recordings")
public class RecordingController {

    private final RecordingService recordingService;

    /** ----------------------------------------------------
     *  0. 녹음 인증 (병원 인증 코드 확인)
     *  POST /api/recordings/{consultationId}/authorize
     *  Body: { "authCode": "xxxx" }
     * ---------------------------------------------------- */
    @PostMapping("/{consultationId}/authorize")
    public ResponseEntity<Void> authorizeRecording(
            @PathVariable Long consultationId,
            @RequestBody Map<String, String> req
    ) {
        String authCode = req.get("authCode");
        recordingService.authorizeRecording(consultationId, authCode);
        return ResponseEntity.ok().build();
    }

     *  1. 녹음 상세 조회
     *  GET /api/recordings/{recordingId}
     * ---------------------------------------------------- */
    @GetMapping("/{recordingId}")
    public ResponseEntity<RecordingDetailResponseDto> getRecording(
            @PathVariable Long recordingId
    ) {
        return ResponseEntity.ok(recordingService.getRecording(recordingId));
    }

    /** ----------------------------------------------------
     *  2. 녹음 업로드
     *  POST /api/recordings/{consultationId}
     *  Body: multipart/form-data
     *  Swagger에서 파일 업로드 UI 뜨게 설정
     * ---------------------------------------------------- */
    @PostMapping(
            value = "/{consultationId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<RecordingDetailResponseDto> uploadRecording(
            @PathVariable Long consultationId,
            @RequestParam("file") MultipartFile file
    ) {
        RecordingDetailResponseDto dto =
                recordingService.uploadRecording(consultationId, file);

        return ResponseEntity.ok(dto);
    }

    /** ----------------------------------------------------
     *  3. STT 실행
     *  POST /api/recordings/{recordingId}/stt
     * ---------------------------------------------------- */
    @PostMapping("/{recordingId}/stt")
    public ResponseEntity<Void> runStt(
            @PathVariable Long recordingId
    ) {
        recordingService.runStt(recordingId);
        return ResponseEntity.ok().build();
    }

    /** ----------------------------------------------------
     *  4. transcript 직접 수정
     *  PATCH /api/recordings/{recordingId}/transcript
     *  Body: "텍스트 그대로"
     * ---------------------------------------------------- */
    @PatchMapping("/{recordingId}/transcript")
    public ResponseEntity<Void> updateTranscript(
            @PathVariable Long recordingId,
            @RequestBody String transcript
    ) {
        recordingService.updateTranscript(recordingId, transcript);
        return ResponseEntity.ok().build();
    }

    /** ----------------------------------------------------
     *  5. 상태 변경
     *  PATCH /api/recordings/{recordingId}/status
     *  Body: "TRANSCRIBED"
     * ---------------------------------------------------- */
    @PatchMapping("/{recordingId}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long recordingId,
            @RequestBody String statusStr
    ) {
        RecordingStatus status = RecordingStatus.valueOf(statusStr);
        recordingService.updateStatus(recordingId, status);

        return ResponseEntity.ok().build();
    }
}
