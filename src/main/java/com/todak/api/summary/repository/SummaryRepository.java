package com.todak.api.summary.repository;

import com.todak.api.summary.entity.Summary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SummaryRepository extends JpaRepository<Summary, Long> {

    // 가장 최신 Summary 1개 조회 (상세 화면에서 사용)
    Optional<Summary> findTopByConsultationIdOrderByCreatedAtDesc(Long consultationId);

    // 기록을 볼 일이 생긴다면 전체 이력 조회도 가능
    List<Summary> findByConsultationIdOrderByCreatedAtDesc(Long consultationId);

    // 녹음ID 기반 최신 Summary 조회 (녹음 파일 화면에서 필요할 수 있음)
    Optional<Summary> findTopByRecordingIdOrderByCreatedAtDesc(Long recordingId);
}
