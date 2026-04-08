package com.sogong.todak.recording.controller;

import com.sogong.todak.recording.dto.request.MemoUpdateRequest;
import com.sogong.todak.recording.dto.response.MyRecordingListResponse;
import com.sogong.todak.recording.dto.response.RecentRecordingResponse;
import com.sogong.todak.recording.dto.response.RecordingDetailResponse;
import com.sogong.todak.recording.service.RecordingListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/recordings")
@RequiredArgsConstructor
public class RecordingListController {

    private final RecordingListService recordingListService;

    private UUID extractUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        Object principal = authentication.getPrincipal();

        // 1. 토큰 해석본이 직접 UUID로 들어오는 경우
        if (principal instanceof UUID) {
            return (UUID) principal;
        }

        // 2. 토큰 해석본이 String(이름) 형태로 들어오는 경우
        try {
            return UUID.fromString(authentication.getName());
        } catch (Exception e) {
            throw new IllegalArgumentException("토큰에서 유저 ID를 추출할 수 없습니다.");
        }
    }

    // 1. 녹음 리스트 조회 - 내 진료
    @GetMapping("/list/my")
    public ResponseEntity<?> getMyRecordings(Authentication authentication) {
        UUID userId = extractUserId(authentication);
        List<MyRecordingListResponse> data = recordingListService.getMyRecordingList(userId);
        return ResponseEntity.ok(Map.of("status", 200, "message", "내 진료 목록 조회에 성공했습니다.", "data", data));
    }

    // 2. 최근 진료 기록 조회
    @GetMapping("/recent")
    public ResponseEntity<?> getRecentRecordings(Authentication authentication) {
        UUID userId = extractUserId(authentication);
        List<RecentRecordingResponse> data = recordingListService.getRecentRecordings(userId);
        return ResponseEntity.ok(Map.of("status", 200, "message", "최근 진료 기록 조회에 성공했습니다.", "data", data));
    }

    // 3. 녹음 상세 조회 - 내 진료
    @GetMapping("/{recordingId}")
    public ResponseEntity<?> getRecordingDetail(
            @PathVariable UUID recordingId,
            Authentication authentication) {
        UUID userId = extractUserId(authentication);
        RecordingDetailResponse data = recordingListService.getRecordingDetail(recordingId, userId);
        return ResponseEntity.ok(Map.of("status", 200, "message", "녹음 상세 조회에 성공했습니다.", "data", data));
    }

    // 4. 녹음 내역 삭제
    @DeleteMapping("/{recordingId}")
    public ResponseEntity<?> deleteRecording(
            @PathVariable UUID recordingId,
            Authentication authentication) {
        UUID userId = extractUserId(authentication);
        recordingListService.deleteRecording(recordingId, userId);
        return ResponseEntity.ok(Map.of("status", 200, "message", "진료 기록이 정상적으로 삭제되었습니다."));
    }

    // 5. 녹음 메모 추가/수정
    @PatchMapping("/{recordingId}/memo")
    public ResponseEntity<?> updateMemo(
            @PathVariable UUID recordingId,
            @RequestBody MemoUpdateRequest request,
            Authentication authentication) {
        UUID userId = extractUserId(authentication);
        recordingListService.updateMemo(recordingId, userId, request);
        return ResponseEntity.ok(Map.of("status", 200, "message", "메모가 성공적으로 저장되었습니다."));
    }

    // 6. 녹음 메모 제거
    @DeleteMapping("/{recordingId}/memo")
    public ResponseEntity<?> deleteMemo(
            @PathVariable UUID recordingId,
            Authentication authentication) {
        UUID userId = extractUserId(authentication);
        recordingListService.deleteMemo(recordingId, userId);
        return ResponseEntity.ok(Map.of("status", 200, "message", "메모가 성공적으로 삭제되었습니다."));
    }
}