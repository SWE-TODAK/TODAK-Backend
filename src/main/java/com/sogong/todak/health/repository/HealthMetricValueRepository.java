package com.sogong.todak.health.repository;

import com.sogong.todak.health.entity.HealthMetricValue;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HealthMetricValueRepository extends JpaRepository<HealthMetricValue, UUID> {
    // 특정 지표의 최신 기록을 limit 개수만큼 조회 (그래프용)
    List<HealthMetricValue> findByHealthMetric_IdOrderByRecordedAtDesc(UUID metricId, Pageable pageable);
}