// src/main/java/com/todak/api/summary/repository/SummaryRepository.java
package com.todak.api.summary.repository;

import com.todak.api.summary.entity.Summary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SummaryRepository extends JpaRepository<Summary, Long> {

    Optional<Summary> findByConsultationId(Long consultationId);
}

