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
import java.time.ZoneId;
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

    // 상수로 매직 스트링 관리 (오타 방지)
    private static final String TYPE_BLOOD_PRESSURE = "BLOOD_PRESSURE";
    private static final String TYPE_WEIGHT = "WEIGHT";
    private static final String TYPE_BLOOD_SUGAR = "BLOOD_SUGAR";
    private static final String TYPE_CHOLESTEROL = "CHOLESTEROL";
    private static final String TYPE_HEART = "HEART";
    private static final String TYPE_LIVER = "LIVER";
    private static final String TYPE_KIDNEY = "KIDNEY";
    private static final String TYPE_CUSTOM = "CUSTOM";

    // 한국 시간대 고정 설정
    private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");

    private static class DefaultMetricInfo {
        String metricId;
        String name;
        String unit;
        String metricType;

        DefaultMetricInfo(String metricId, String name, String unit, String metricType) {
            this.metricId = metricId;
            this.name = name;
            this.unit = unit;
            this.metricType = metricType;
        }
    }

    private static final List<DefaultMetricInfo> DEFAULT_METRICS = List.of(
            new DefaultMetricInfo("m-1", "혈압", "mmHg", TYPE_BLOOD_PRESSURE),
            new DefaultMetricInfo("m-2", "체중", "kg", TYPE_WEIGHT),
            new DefaultMetricInfo("m-3", "혈당", "mg/dL", TYPE_BLOOD_SUGAR),
            new DefaultMetricInfo("m-4", "콜레스테롤", "mg/dL", TYPE_CHOLESTEROL),
            new DefaultMetricInfo("m-5", "심박수", "bpm", TYPE_HEART),
            new DefaultMetricInfo("m-6", "간 수치", "IU/L", TYPE_LIVER),
            new DefaultMetricInfo("m-7", "신장 수치", "mg/dL", TYPE_KIDNEY)
    );

    private DefaultMetricInfo getDefaultMetricInfo(String metricId) {
        return DEFAULT_METRICS.stream()
                .filter(m -> m.metricId.equals(metricId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("잘못된 기본 지표 ID입니다: " + metricId));
    }

    // 1. 나의 건강지표 목록 조회
    @Transactional(readOnly = true)
    public List<HealthMetricResponse> getMyMetrics(UUID userId) {
        List<HealthMetricResponse> responses = new ArrayList<>();

        for (DefaultMetricInfo info : DEFAULT_METRICS) {
            responses.add(HealthMetricResponse.builder()
                    .metricId(info.metricId)
                    .name(info.name)
                    .unit(info.unit)
                    .isCustom(false)
                    .metricType(info.metricType)
                    .build());
        }

        List<HealthMetric> customMetrics = metricRepository.findAllByUser_UserId(userId).stream()
                .filter(HealthMetric::isCustom)
                .collect(Collectors.toList());

        for (HealthMetric m : customMetrics) {
            responses.add(HealthMetricResponse.builder()
                    .metricId(m.getId().toString())
                    .name(m.getName())
                    .unit(m.getUnit())
                    .isCustom(true)
                    .metricType(m.getMetricType())
                    .build());
        }

        return responses;
    }

    // 2. 건강 수치 추가
    @Transactional
    public UUID recordValue(UUID userId, RecordValueRequest request) {
        HealthMetric metric = resolveMetricForUser(userId, request.getMetricId());

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
                // 한국 시간대 명시적 적용
                .recordedAt(request.getDate() != null ? request.getDate() : LocalDateTime.now(KST_ZONE))
                .build();

        return valueRepository.save(value).getId();
    }

    // 3. 건강 수치 추이 조회
    @Transactional(readOnly = true)
    public MetricQueryResponse getMetricHistory(UUID userId, String reqMetricId, int limit) {
        HealthMetric metric;
        String metricType;

        if (reqMetricId.startsWith("m-")) {
            DefaultMetricInfo defaultInfo = getDefaultMetricInfo(reqMetricId);
            metricType = defaultInfo.metricType;

            metric = metricRepository.findAllByUser_UserId(userId).stream()
                    .filter(m -> !m.isCustom() && m.getMetricType().equals(defaultInfo.metricType))
                    .findFirst()
                    .orElse(null);

            if (metric == null) {
                return MetricQueryResponse.builder()
                        .metricType(metricType)
                        .history(new ArrayList<>())
                        .build();
            }
        } else {
            metric = metricRepository.findById(UUID.fromString(reqMetricId))
                    .orElseThrow(() -> new IllegalArgumentException("지표를 찾을 수 없습니다."));

            if (!metric.getUser().getUserId().equals(userId)) {
                throw new IllegalArgumentException("본인의 지표만 조회할 수 있습니다.");
            }
            metricType = metric.getMetricType();
        }

        List<HealthMetricValue> values = valueRepository.findByHealthMetric_IdOrderByRecordedAtDesc(
                metric.getId(), PageRequest.of(0, limit));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd/E", Locale.KOREAN);

        List<MetricQueryResponse.HistoryDto> history = values.stream().map(v ->
                MetricQueryResponse.HistoryDto.builder()
                        .metricValueId(v.getId())
                        .metricId(reqMetricId)
                        .date(v.getRecordedAt().format(formatter))
                        .systolic(v.getSystolic())
                        .diastolic(v.getDiastolic())
                        .value(v.getValue())
                        .status("NORMAL")
                        .build()
        ).collect(Collectors.toList());

        return MetricQueryResponse.builder()
                .metricType(metricType)
                .history(history)
                .build();
    }

    // 4. 나만의 건강지표 수동 생성
    @Transactional
    public UUID createCustomMetric(UUID userId, CreateMetricRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        HealthMetric metric = HealthMetric.builder()
                .user(user)
                .name(request.getName())
                .unit(request.getUnit())
                .isCustom(true)
                .metricType(request.getMetricType() != null ? request.getMetricType() : TYPE_CUSTOM)
                .build();

        return metricRepository.save(metric).getId();
    }

    // 5. 상세 정보 조회
    @Transactional(readOnly = true)
    public MetricDetailResponse getMetricDetail(UUID valueId) {
        HealthMetricValue v = valueRepository.findById(valueId)
                .orElseThrow(() -> new IllegalArgumentException("해당 기록을 찾을 수 없습니다."));

        HealthMetric m = v.getHealthMetric();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd EEEE", Locale.KOREAN);

        List<ValueLabelDto> labels = new ArrayList<>();
        String type = m.getMetricType();

        if (TYPE_BLOOD_PRESSURE.equalsIgnoreCase(type)) {
            labels.add(new ValueLabelDto("수축기", v.getSystolic() != null ? v.getSystolic().doubleValue() : 0.0));
            labels.add(new ValueLabelDto("이완기", v.getDiastolic() != null ? v.getDiastolic().doubleValue() : 0.0));
        } else if (TYPE_BLOOD_SUGAR.equalsIgnoreCase(type)) {
            labels.add(new ValueLabelDto("공복 혈당", v.getBeforeMeal() != null ? v.getBeforeMeal().doubleValue() : 0.0));
            labels.add(new ValueLabelDto("식후 혈당", v.getAfterMeal() != null ? v.getAfterMeal().doubleValue() : 0.0));
        } else if (TYPE_CHOLESTEROL.equalsIgnoreCase(type)) {
            labels.add(new ValueLabelDto("총 콜레스테롤", v.getTotalChol() != null ? v.getTotalChol() : 0.0));
            labels.add(new ValueLabelDto("중성지방", v.getTriglyceride() != null ? v.getTriglyceride() : 0.0));
        } else {
            labels.add(new ValueLabelDto(m.getName(), v.getValue() != null ? v.getValue() : 0.0));
        }

        return MetricDetailResponse.builder()
                .metricValueId(v.getId().toString())
                .metricType(type)
                .recordedAt(v.getRecordedAt().format(formatter))
                .unit(m.getUnit())
                .values(labels)
                .build();
    }

    // 6. 건강 지표 삭제
    @Transactional
    public void deleteMetric(UUID userId, String reqMetricId) {
        if (reqMetricId.startsWith("m-")) {
            throw new IllegalArgumentException("기본 제공 지표 자체는 삭제할 수 없습니다. (개별 수치 삭제를 이용해 주세요)");
        }

        HealthMetric metric = metricRepository.findById(UUID.fromString(reqMetricId))
                .orElseThrow(() -> new IllegalArgumentException("지표를 찾을 수 없습니다."));

        if (!metric.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 건강 지표만 삭제할 수 있습니다.");
        }

        valueRepository.deleteByHealthMetric_Id(metric.getId());
        metricRepository.delete(metric);
    }

    // 7. 건강 수치 삭제
    @Transactional
    public void deleteMetricValue(UUID userId, UUID valueId) {
        HealthMetricValue value = valueRepository.findById(valueId)
                .orElseThrow(() -> new IllegalArgumentException("기록을 찾을 수 없습니다."));

        if (!value.getHealthMetric().getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 건강 기록만 삭제할 수 있습니다.");
        }

        valueRepository.delete(value);
    }

    private HealthMetric resolveMetricForUser(UUID userId, String reqMetricId) {
        if (reqMetricId != null && reqMetricId.startsWith("m-")) {
            DefaultMetricInfo defaultInfo = getDefaultMetricInfo(reqMetricId);

            return metricRepository.findAllByUser_UserId(userId).stream()
                    .filter(m -> !m.isCustom() && m.getMetricType().equals(defaultInfo.metricType))
                    .findFirst()
                    .orElseGet(() -> {
                        User user = userRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
                        HealthMetric newMetric = HealthMetric.builder()
                                .user(user)
                                .name(defaultInfo.name)
                                .unit(defaultInfo.unit)
                                .isCustom(false)
                                .metricType(defaultInfo.metricType)
                                .build();
                        return metricRepository.save(newMetric);
                    });
        } else {
            HealthMetric metric = metricRepository.findById(UUID.fromString(reqMetricId))
                    .orElseThrow(() -> new IllegalArgumentException("지표를 찾을 수 없습니다."));

            if (!metric.getUser().getUserId().equals(userId)) {
                throw new IllegalArgumentException("본인의 건강 지표에만 수치를 추가할 수 있습니다.");
            }
            return metric;
        }
    }
}