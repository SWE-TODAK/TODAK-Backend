package com.sogong.todak.health.service;

import com.sogong.todak.health.dto.response.MetricDetailResponse;
import com.sogong.todak.health.dto.response.MetricDetailResponse.ValueLabelDto;
import com.sogong.todak.health.dto.response.MetricQueryResponse;
import com.sogong.todak.health.entity.HealthMetric;
import com.sogong.todak.health.entity.HealthMetricValue;
import com.sogong.todak.health.repository.HealthMetricRepository;
import com.sogong.todak.health.repository.HealthMetricValueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HealthService {

    private final HealthMetricRepository metricRepository; // 추가됨
    private final HealthMetricValueRepository valueRepository;

    @Transactional(readOnly = true)
    public MetricDetailResponse getMetricDetail(UUID valueId) {
        HealthMetricValue v = valueRepository.findById(valueId)
                .orElseThrow(() -> new IllegalArgumentException("해당 기록을 찾을 수 없습니다."));

        HealthMetric m = v.getHealthMetric();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd EEEE", Locale.KOREAN);

        List<ValueLabelDto> labels = new ArrayList<>();
        String type = m.getMetricType();

        if ("BLOOD_PRESSURE".equalsIgnoreCase(type)) {
            labels.add(new ValueLabelDto("수축기", v.getSystolic()));
            labels.add(new ValueLabelDto("이완기", v.getDiastolic()));
        } else if ("BLOOD_SUGAR".equalsIgnoreCase(type)) {
            labels.add(new ValueLabelDto("공복 혈당", v.getBeforeMeal()));
            labels.add(new ValueLabelDto("식후 혈당", v.getAfterMeal()));
        } else if ("CHOLESTEROL".equalsIgnoreCase(type)) {
            labels.add(new ValueLabelDto("총 콜레스테롤", v.getTotalChol()));
            labels.add(new ValueLabelDto("중성지방", v.getTriglyceride()));
        } else {
            labels.add(new ValueLabelDto(m.getName(), v.getValue()));
        }

        return MetricDetailResponse.builder()
                .metricValueId(v.getId().toString())
                .metricType(type)
                .recordedAt(v.getRecordedAt().format(formatter))
                .unit(m.getUnit())
                .values(labels)
                .build();
    }

    @Transactional(readOnly = true)
    public MetricQueryResponse getMetricHistory(UUID metricId, int limit) {
        HealthMetric metric = metricRepository.findById(metricId)
                .orElseThrow(() -> new IllegalArgumentException("지표를 찾을 수 없습니다."));

        List<HealthMetricValue> values = valueRepository.findByHealthMetric_IdOrderByRecordedAtDesc(
                metricId, PageRequest.of(0, limit));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd/E", Locale.KOREAN);

        List<MetricQueryResponse.HistoryDto> history = values.stream().map(v ->
                MetricQueryResponse.HistoryDto.builder()
                        .metricId(v.getId().toString())
                        .date(v.getRecordedAt().format(formatter))
                        .systolic(v.getSystolic())
                        .diastolic(v.getDiastolic())
                        .value(v.getValue())
                        .status("NORMAL")
                        .build()
        ).collect(Collectors.toList());

        return MetricQueryResponse.builder()
                .metricType(metric.getMetricType())
                .history(history)
                .build();
    }
}