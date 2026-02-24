package com.sogong.todak.health.repository;

import com.sogong.todak.health.entity.HealthMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HealthMetricRepository extends JpaRepository<HealthMetric, UUID> {
    // 특정 유저의 지표 목록 조회
    List<HealthMetric> findAllByUser_UserId(UUID userId);
}