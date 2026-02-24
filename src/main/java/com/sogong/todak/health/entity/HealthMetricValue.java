package com.sogong.todak.health.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "health_metric_values")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class HealthMetricValue {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "metric_id", nullable = false)
    private HealthMetric healthMetric;

    private Double value;        // 일반 수치 (몸무게 등)

    // 혈압용
    private Integer systolic;
    private Integer diastolic;

    // 혈당용
    private Integer beforeMeal;
    private Integer afterMeal;

    // 지질·콜레스테롤용 (추가된 필드)
    private Double totalChol;     // 총 콜레스테롤
    private Double triglyceride;  // 중성지방
    private Double hdl;
    private Double ldl;

    @Column(nullable = false)
    private LocalDateTime recordedAt;
}