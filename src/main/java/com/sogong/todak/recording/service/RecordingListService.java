package com.sogong.todak.recording.service;

import com.sogong.todak.job.repository.JobRepository;
import com.sogong.todak.recording.dto.request.MemoUpdateRequest;
import com.sogong.todak.recording.dto.response.MyRecordingListResponse;
import com.sogong.todak.recording.dto.response.RecentRecordingResponse;
import com.sogong.todak.recording.dto.response.RecordingDetailResponse;
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
    private final JobRepository jobRepository;

    public List<MyRecordingListResponse> getMyRecordingList(UUID userId) {
        return recordingRepository.findAllWithSummaryByUserId(userId)
                .stream().map(MyRecordingListResponse::from).collect(Collectors.toList());
    }

    public List<RecentRecordingResponse> getRecentRecordings(UUID userId) {
        return recordingRepository.findTop4ByUser_UserIdOrderByCreatedAtDesc(userId)
                .stream().map(RecentRecordingResponse::from).collect(Collectors.toList());
    }

    public RecordingDetailResponse getRecordingDetail(UUID recordingId, UUID userId) {
        // N+1 방지용 최적화 메서드 호출
        Recording recording = recordingRepository.findWithDetailsByRecordingIdAndUser_UserId(recordingId, userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 녹음 기록을 찾을 수 없거나 권한이 없습니다."));
        return RecordingDetailResponse.from(recording);
    }

    @Transactional
    public void deleteRecording(UUID recordingId, UUID userId) {
        Recording recording = getRecordingOrThrow(recordingId, userId);

        // ✅ 1. S3 실제 파일 삭제 로직 호출 위치 (TODO: S3Service 연동 필요)

        // 연관된 Job 레코드 삭제 (FK 제약조건 위반 방지)
        jobRepository.deleteByRecordingId(recordingId);

        // 녹음 메타데이터 삭제
        recordingRepository.delete(recording);
    }

    @Transactional
    public void updateMemo(UUID recordingId, UUID userId, MemoUpdateRequest request) {
        Recording recording = getRecordingOrThrow(recordingId, userId);
        recording.updateMemo(request.getMemo());
    }

    @Transactional
    public void deleteMemo(UUID recordingId, UUID userId) {
        Recording recording = getRecordingOrThrow(recordingId, userId);
        recording.deleteMemo();
    }

    private Recording getRecordingOrThrow(UUID recordingId, UUID userId) {
        return recordingRepository.findByRecordingIdAndUser_UserId(recordingId, userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 녹음 기록을 찾을 수 없거나 권한이 없습니다."));
    }
}