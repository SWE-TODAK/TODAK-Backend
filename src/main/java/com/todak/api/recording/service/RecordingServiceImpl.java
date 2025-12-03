package com.todak.api.recording.service;

import com.todak.api.consultation.entity.Consultation;
import com.todak.api.consultation.repository.ConsultationRepository;
import com.todak.api.infra.ai.AiClient;
import com.todak.api.infra.ai.dto.AiSttResponseDto;
import com.todak.api.infra.s3.S3UploaderService;
import com.todak.api.recording.dto.response.RecordingDetailResponseDto;
import com.todak.api.recording.entity.Recording;
import com.todak.api.recording.entity.RecordingStatus;
import com.todak.api.recording.repository.RecordingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class RecordingServiceImpl implements RecordingService {

    private final RecordingRepository recordingRepository;
    private final ConsultationRepository consultationRepository;
    private final S3UploaderService s3Uploader;
    private final AiClient aiClient;

    /** ----------------------------------------------------
     *  1. 녹음 상세 조회
     * ---------------------------------------------------- */
    @Override
    public RecordingDetailResponseDto getRecording(Long id) {
        Recording r = recordingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recording not found: " + id));

        return RecordingDetailResponseDto.from(r);
    }

    /** ----------------------------------------------------
     *  2. 녹음 파일 업로드 (클라이언트 → Spring)
     * ---------------------------------------------------- */
    @Override
    public RecordingDetailResponseDto uploadRecording(Long consultationId, MultipartFile file) {

        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new IllegalArgumentException("consultation not found: " + consultationId));

        // S3 업로드 (URL 아니라 key 반환)
        String key = s3Uploader.upload(file, "recordings");

        // 확장자 파싱
        String extension = extractExtension(file.getOriginalFilename());

        // Recording 생성
        Recording recording = Recording.builder()
                .consultation(consultation)
                .patient(consultation.getPatient())
                .hospital(consultation.getHospital())
                .filePath(key)   // key 저장
                .format(extension)
                .fileSizeMb((double) file.getSize() / (1024 * 1024))
                .status(RecordingStatus.UPLOADED)
                .build();

        recordingRepository.save(recording);

        return RecordingDetailResponseDto.from(recording);
    }

    /** ----------------------------------------------------
     *  3. STT 실행 (Spring → AI 서버)
     * ---------------------------------------------------- */
    @Override
    public void runStt(Long recordingId) {

        Recording recording = recordingRepository.findById(recordingId)
                .orElseThrow(() -> new IllegalArgumentException("Recording not found: " + recordingId));

        // S3에서 key 기반 다운로드
        MultipartFile audioFile = s3Uploader.downloadAsMultipartFileByKey(recording.getFilePath());

        // AI 서버에 Whisper STT 요청
        AiSttResponseDto aiRes = aiClient.requestStt(
                recording.getRecordingId(),
                recording.getConsultation().getConsultationId(),
                audioFile
        );

        // 결과 저장
        recording.setTranscript(aiRes.getData().getTranscript());
        recording.setDurationSeconds(aiRes.getData().getDuration());
        recording.setStatus(RecordingStatus.TRANSCRIBED);

        recordingRepository.save(recording);
    }

    /** ----------------------------------------------------
     *  4. transcript 직접 수정
     * ---------------------------------------------------- */
    @Override
    public void updateTranscript(Long id, String transcript) {
        Recording r = recordingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recording not found"));

        r.setTranscript(transcript);
        r.setStatus(RecordingStatus.TRANSCRIBED);

        recordingRepository.save(r);
    }

    /** ----------------------------------------------------
     *  5. 상태 변경
     * ---------------------------------------------------- */
    @Override
    public void updateStatus(Long id, RecordingStatus status) {
        Recording r = recordingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recording not found"));

        r.setStatus(status);
        recordingRepository.save(r);
    }

    /** ----------------------------------------------------
     *  6. Consultation → Recording 조회
     * ---------------------------------------------------- */
    @Override
    public Recording getRecordingByConsultation(Long consultationId) {
        return recordingRepository.findFirstByConsultation_ConsultationId(consultationId)
                .orElseThrow(() -> new IllegalArgumentException("recording not found for consultation: " + consultationId));
    }

    /** ----------------------------------------------------
     *  Private helpers
     * ---------------------------------------------------- */
    private String extractExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) return null;
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    @Override
    public void authorizeRecording(Long consultationId, String authCode) {

        // 1) 진료 조회
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new IllegalArgumentException("consultation not found: " + consultationId));

        // 2) 병원에서 설정해놓은 녹음 허가 코드랑 비교
        // 👉 병원 엔티티에 실제 필드명에 맞춰서 수정해줘야 함.
        String hospitalCode = consultation.getHospital().getHospitalAuthKey();
        // 예: getRecordingAuthCode() / getRecordingCode() / getAuthCode() 등 실제 네이밍 맞춰서

        if (hospitalCode == null || !hospitalCode.equals(authCode)) {
            throw new IllegalArgumentException("녹음 허가 코드가 일치하지 않습니다.");
        }

        // 3) 여기서는 “인증 끝났음”만 기록하면 됨.
        //    방법 A: 녹음 엔티티 상태로 관리
        //       - consultationId 기준으로 녹음을 찾고, 없으면 새로 만들고, status=AUTHORIZED
        //    방법 B: Consultation에 boolean 플래그 저장 (예: isRecordingAuthorized = true)

        // 🔹 방법 A 예시: RecordingStatus.AUTHORIZED 쓰는 방식

        // consultation에 대해 기존 녹음이 있는지 먼저 찾아봄
        Recording recording = recordingRepository
                .findFirstByConsultation_ConsultationId(consultationId)
                .orElse(null);

        if (recording == null) {
            // 아직 녹음 레코드가 없으면 “허가만 된 상태”로 빈 녹음 하나 만들어둘 수도 있음
            recording = Recording.builder()
                    .consultation(consultation)
                    .hospital(consultation.getHospital())
                    .status(RecordingStatus.AUTHORIZED) // ✅ 허가만 받은 상태
                    .build();
        } else {
            // 기존 녹음이 있으면 상태만 AUTHORIZED 로 올려줌
            recording.setStatus(RecordingStatus.AUTHORIZED);
        }

        recordingRepository.save(recording);
    }
}
