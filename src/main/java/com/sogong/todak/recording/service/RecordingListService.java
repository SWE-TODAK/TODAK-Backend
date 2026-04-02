package com.sogong.todak.recording.service;

import com.sogong.todak.recording.dto.request.MemoUpdateRequest;
import com.sogong.todak.recording.dto.response.MyRecordingListResponse;
import com.sogong.todak.recording.dto.response.RecentRecordingResponse;
import com.sogong.todak.recording.dto.response.RecordingDetailResponse;
import com.sogong.todak.recording.dto.response.RecordingListResponse;
import com.sogong.todak.recording.entity.Recording;
import com.sogong.todak.recording.repository.RecordingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecordingListService {

    private final RecordingRepository recordingRepository;

    // 1. 내 진료 기록 리스트 조회
    public List<MyRecordingListResponse> getMyRecordingList(UUID userId) {
        return recordingRepository.findAllWithSummaryByUserId(userId)
                .stream()
                .map(MyRecordingListResponse::from)
                .collect(Collectors.toList());
    }

    // 2. 최근 진료 기록 조회
    @Transactional(readOnly = true)
    public List<RecentRecordingResponse> getRecentRecordings(UUID userId) {
        // 레포지토리에서 최대 4개를 Summary와 함께 조회
        return recordingRepository.findTop4ByUser_UserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(RecentRecordingResponse::from)
                .collect(Collectors.toList());
    }

    // 3. 녹음 상세 조회
    public RecordingDetailResponse getRecordingDetail(UUID recordingId, UUID userId) {
        Recording recording = getRecordingOrThrow(recordingId, userId);
        return RecordingDetailResponse.from(recording);
    }

    // 4. 녹음 내역 삭제
    @Transactional
    public void deleteRecording(UUID recordingId, UUID userId) {
        Recording recording = getRecordingOrThrow(recordingId, userId);
        recordingRepository.delete(recording);
    }

    // 5. 녹음 메모 추가/수정
    @Transactional
    public void updateMemo(UUID recordingId, UUID userId, MemoUpdateRequest request) {
        Recording recording = getRecordingOrThrow(recordingId, userId);
        recording.updateMemo(request.getMemo());
    }

    // 6. 녹음 메모 제거
    @Transactional
    public void deleteMemo(UUID recordingId, UUID userId) {
        Recording recording = getRecordingOrThrow(recordingId, userId);
        recording.deleteMemo();
    }

    private Recording getRecordingOrThrow(UUID recordingId, UUID userId) {
        return recordingRepository.findByRecordingIdAndUser_UserId(recordingId, userId) // 💡 올바른 이름 호출
                .orElseThrow(() -> new IllegalArgumentException("해당 녹음 기록을 찾을 수 없거나 권한이 없습니다."));
    }
}