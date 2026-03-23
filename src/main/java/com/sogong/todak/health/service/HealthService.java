package com.sogong.todak.health.service;

import com.sogong.todak.health.dto.request.CreateMetricRequest;
import com.sogong.todak.health.dto.request.RecordValueRequest;
import com.sogong.todak.health.dto.response.HealthMetricResponse;
import com.sogong.todak.health.dto.response.MetricDetailResponse;
import com.sogong.todak.health.dto.response.MetricDetailResponse.ValueLabelDto;
import com.sogong.todak.health.dto.response.MetricQueryResponse;
import com.sogong.todak.health.entity.HealthMetric;
import com.sogong.todak.health.entity.HealthMetricValue;
import com.sogong.todak.health.repository.HealthMetricRepository;
import com.sogong.todak.health.repository.HealthMetricValueRepository;
import com.sogong.todak.user.entity.User;
import com.sogong.todak.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HealthService {

    private final HealthMetricRepository metricRepository;
    private final HealthMetricValueRepository valueRepository;
    private final UserRepository userRepository;

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

    // 1. 건강 수치 추가
    @Transactional
    public UUID recordValue(UUID userId, RecordValueRequest request) {
        UUID metricId = request.getMetricId() instanceof String ?
                UUID.fromString((String)(Object)request.getMetricId()) :
                (UUID)(Object)request.getMetricId();

        HealthMetric metric = metricRepository.findById(metricId)
                .orElseThrow(() -> new IllegalArgumentException("지표를 찾을 수 없습니다."));


        if (!metric.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 건강 지표에만 수치를 추가할 수 있습니다.");
        }

        HealthMetricValue value = HealthMetricValue.builder()
                .healthMetric(metric)
                .value(request.getValue())
                .systolic(request.getSystolic())
                .diastolic(request.getDiastolic())
                .beforeMeal(request.getBeforeMeal())
                .afterMeal(request.getAfterMeal())
                .totalChol(request.getTotalChol())
                .triglyceride(request.getTriglyceride())
                .hdl(request.getHdl())
                .ldl(request.getLdl())
                .recordedAt(request.getRecordedAt() != null ? request.getRecordedAt() : LocalDateTime.now())
                .build();

        return valueRepository.save(value).getId();
    }

    // 2. 나의 건강지표 목록 조회
    @Transactional(readOnly = true)
    public List<HealthMetricResponse> getMyMetrics(UUID userId) {
        return metricRepository.findAllByUser_UserId(userId).stream()
                .map(m -> HealthMetricResponse.builder()
                        .metricId(m.getId().toString())
                        .name(m.getName())
                        .unit(m.getUnit())
                        .isCustom(m.isCustom())
                        .metricType(m.getMetricType())
                        .build())
                .collect(Collectors.toList());
    }

    // 3. 나만의 건강지표 수동 생성
    @Transactional
    public UUID createCustomMetric(UUID userId, CreateMetricRequest request) {
        User user = userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        HealthMetric metric = HealthMetric.builder()
                .user(user)
                .name(request.getName())
                .unit(request.getUnit())
                .isCustom(true)
                .metricType(request.getMetricType() != null ? request.getMetricType() : "CUSTOM")
                .build();

        return metricRepository.save(metric).getId();
    }

    // 4. 건강 지표 삭제
    @Transactional
    public void deleteMetric(UUID userId, UUID metricId) {
        HealthMetric metric = metricRepository.findById(metricId)
                .orElseThrow(() -> new IllegalArgumentException("지표를 찾을 수 없습니다."));

        if (!metric.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 건강 지표만 삭제할 수 있습니다.");
        }

        valueRepository.deleteByHealthMetric_Id(metricId);
        metricRepository.delete(metric);
    }

    // 5. 건강 수치 삭제
    @Transactional
    public void deleteMetricValue(UUID userId, UUID valueId) {
        HealthMetricValue value = valueRepository.findById(valueId)
                .orElseThrow(() -> new IllegalArgumentException("기록을 찾을 수 없습니다."));

        if (!value.getHealthMetric().getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 건강 기록만 삭제할 수 있습니다.");
        }

        valueRepository.delete(value);
    }
}
