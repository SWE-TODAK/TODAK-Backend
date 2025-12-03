package com.todak.api.recording.service;

import com.todak.api.recording.dto.response.RecordingDetailResponseDto;
import com.todak.api.recording.entity.Recording;
import com.todak.api.recording.entity.RecordingStatus;
import org.springframework.web.multipart.MultipartFile;

public interface RecordingService {

    /**
     * 녹음 상세 조회
     */
    RecordingDetailResponseDto getRecording(Long recordingId);

    /**
     * 녹음 업로드 (클라이언트가 S3로 전송할 파일 업로드)
     */
    RecordingDetailResponseDto uploadRecording(Long consultationId, MultipartFile file);

    /**
     * STT 실행 (Spring → AI 서버)
     * - AI 서버에 파일 전달
     * - transcript / duration 저장
     * - status = TRANSCRIBED 설정
     * - 프론트에 transcript를 직접 반환하지 않음
     */
    void runStt(Long recordingId);

    /**
     * transcript 직접 변경 (관리자용)
     */
    void updateTranscript(Long recordingId, String transcript);

    /**
     * 상태 변경 (WAITING, UPLOADED, TRANSCRIBED 등)
     */
    void updateStatus(Long recordingId, RecordingStatus status);

    /**
     * 특정 진료(consultation)에 연결된 녹음 조회
     * - 진료 1건당 녹음 1개라고 가정한 구조에서 사용
     */
    Recording getRecordingByConsultation(Long consultationId);

    void authorizeRecording(Long consultationId, String authCode);
}
