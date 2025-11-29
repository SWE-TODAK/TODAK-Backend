// src/main/java/com/todak/api/recording/repository/RecordingRepository.java
package com.todak.api.recording.repository;

import com.todak.api.recording.entity.Recording;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecordingRepository extends JpaRepository<Recording, Long> {

    // 특정 진료에 연결된 녹음 찾기 (나중에 요약 조회용)
    Optional<Recording> findByConsultationId(Long consultationId);

    List<Recording> findByHospital_HospitalId(Long hospitalId);
}


