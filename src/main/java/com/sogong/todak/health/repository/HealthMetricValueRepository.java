package com.sogong.todak.health.repository;

import com.sogong.todak.health.entity.HealthMetricValue;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HealthMetricValueRepository extends JpaRepository<HealthMetricValue, UUID> {
    List<HealthMetricValue> findByHealthMetric_IdOrderByRecordedAtDesc(UUID metricId, Pageable pageable);

    void deleteByHealthMetric_Id(UUID metricId);
}