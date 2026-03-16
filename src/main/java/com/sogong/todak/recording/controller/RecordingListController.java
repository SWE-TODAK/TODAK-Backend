package com.sogong.todak.recording.controller;

import com.sogong.todak.recording.dto.request.MemoUpdateRequest;
import com.sogong.todak.recording.dto.response.RecordingDetailResponse;
import com.sogong.todak.recording.dto.response.RecordingListResponse;
import com.sogong.todak.recording.service.RecordingListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/list")
public class RecordingListController {

    private final RecordingListService recordingListService;

    // 임시로 사용할 로그인 유저 ID (실제로는 Spring Security, JWT 등을 통해 주입받아야 합니다)
    // 예: @AuthenticationPrincipal CustomUserDetails userDetails
    private UUID getCurrentUserId() {
        return UUID.fromString("00000000-0000-0000-0000-000000000000"); // TODO: 수정 필요
    }

    // 1. 녹음 리스트 조회 - 내 진료
    @GetMapping("/my")
    public ResponseEntity<List<RecordingListResponse>> getMyRecordings() {
        UUID userId = getCurrentUserId();
        return ResponseEntity.ok(recordingListService.getMyRecordings(userId));
    }

    // 2. 최근 진료 기록 조회
    @GetMapping("/recent")
    public ResponseEntity<List<RecordingListResponse>> getRecentRecordings() {
        UUID userId = getCurrentUserId();
        return ResponseEntity.ok(recordingListService.getRecentRecordings(userId));
    }

    // 3. 녹음 상세 조회 - 내 진료
    @GetMapping("/{recordingId}")
    public ResponseEntity<RecordingDetailResponse> getRecordingDetail(@PathVariable UUID recordingId) {
        UUID userId = getCurrentUserId();
        return ResponseEntity.ok(recordingListService.getRecordingDetail(recordingId, userId));
    }

    // 4. 녹음 내역 삭제
    @DeleteMapping("/{recordingId}")
    public ResponseEntity<Void> deleteRecording(@PathVariable UUID recordingId) {
        UUID userId = getCurrentUserId();
        recordingListService.deleteRecording(recordingId, userId);
        return ResponseEntity.noContent().build();
    }

    // 5. 녹음 메모 추가/수정
    @PatchMapping("/{recordingId}/memo")
    public ResponseEntity<Void> updateMemo(
            @PathVariable UUID recordingId,
            @RequestBody MemoUpdateRequest request) {
        UUID userId = getCurrentUserId();
        recordingListService.updateMemo(recordingId, userId, request);
        return ResponseEntity.ok().build();
    }

    // 6. 녹음 메모 제거
    @DeleteMapping("/{recordingId}/memo")
    public ResponseEntity<Void> deleteMemo(@PathVariable UUID recordingId) {
        UUID userId = getCurrentUserId();
        recordingListService.deleteMemo(recordingId, userId);
        return ResponseEntity.noContent().build();
    }
}