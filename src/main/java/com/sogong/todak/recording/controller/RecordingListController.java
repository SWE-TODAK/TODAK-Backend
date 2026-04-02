package com.sogong.todak.recording.controller;

import com.sogong.todak.recording.dto.request.MemoUpdateRequest;
import com.sogong.todak.recording.dto.response.MyRecordingListResponse;
import com.sogong.todak.recording.dto.response.RecentRecordingResponse;
import com.sogong.todak.recording.dto.response.RecordingDetailResponse;
import com.sogong.todak.recording.service.RecordingListService;

import com.sogong.todak.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/recordings")
@RequiredArgsConstructor
public class RecordingListController {

    private final RecordingListService recordingListService;

    // 1. 녹음 리스트 조회 - 내 진료
    @GetMapping("/list/my")
    public ResponseEntity<?> getMyRecordings(@AuthenticationPrincipal User user) {
        List<MyRecordingListResponse> data = recordingListService.getMyRecordingList(user.getUserId());
        return ResponseEntity.ok(Map.of("message", "내 진료 목록 조회에 성공했습니다.", "data", data));
    }

    // 2. 최근 진료 기록 조회
    @GetMapping("/recent")
    public ResponseEntity<?> getRecentRecordings(@AuthenticationPrincipal User user) {
        List<RecentRecordingResponse> data = recordingListService.getRecentRecordings(user.getUserId());
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "최근 진료 기록 조회에 성공했습니다.",
                "data", data
        ));
    }

    // 3. 녹음 상세 조회 - 내 진료
    @GetMapping("/{recordingId}")
    public ResponseEntity<RecordingDetailResponse> getRecordingDetail(
            @PathVariable UUID recordingId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(recordingListService.getRecordingDetail(recordingId, user.getUserId()));
    }

    // 4. 녹음 내역 삭제
    @DeleteMapping("/{recordingId}")
    public ResponseEntity<Void> deleteRecording(
            @PathVariable UUID recordingId,
            @AuthenticationPrincipal User user) {
        recordingListService.deleteRecording(recordingId, user.getUserId());
        return ResponseEntity.noContent().build();
    }

    // 5. 녹음 메모 추가/수정
    @PatchMapping("/{recordingId}/memo")
    public ResponseEntity<Void> updateMemo(
            @PathVariable UUID recordingId,
            @RequestBody MemoUpdateRequest request,
            @AuthenticationPrincipal User user) {
        recordingListService.updateMemo(recordingId, user.getUserId(), request);
        return ResponseEntity.ok().build();
    }

    // 6. 녹음 메모 제거
    @DeleteMapping("/{recordingId}/memo")
    public ResponseEntity<Void> deleteMemo(
            @PathVariable UUID recordingId,
            @AuthenticationPrincipal User user) {
        recordingListService.deleteMemo(recordingId, user.getUserId());
        return ResponseEntity.noContent().build();
    }
}