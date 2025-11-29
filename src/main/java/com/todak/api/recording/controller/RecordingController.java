// src/main/java/com/todak/api/recording/controller/RecordingController.java
package com.todak.api.recording.controller;

import com.todak.api.recording.dto.response.RecordingDetailResponseDto;
import com.todak.api.recording.service.RecordingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recordings")
@RequiredArgsConstructor
public class RecordingController {

    private final RecordingService recordingService;

    // 녹음 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<RecordingDetailResponseDto> getRecording(@PathVariable Long id) {
        return ResponseEntity.ok(recordingService.getRecording(id));
    }

    // STT 업데이트 (AI 서버에서 호출)
    @PatchMapping("/{id}/stt")
    public ResponseEntity<String> updateTranscript(
            @PathVariable Long id,
            @RequestBody String transcript
    ) {
        recordingService.updateTranscript(id, transcript);
        return ResponseEntity.ok("STT updated");
    }
}
