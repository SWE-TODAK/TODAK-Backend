package com.sogong.todak.health.service;

import com.sogong.todak.health.dto.request.CreateMetricRequest;
import com.sogong.todak.health.dto.request.RecordValueRequest;
import com.sogong.todak.health.dto.response.HealthMetricResponse;
import com.sogong.todak.health.dto.response.MetricQueryResponse;
import com.sogong.todak.health.dto.response.MetricDetailResponse;
import com.sogong.todak.health.dto.response.MetricDetailResponse.ValueLabelDto;
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
        requireActiveUser(userId);

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
        requireActiveUser(userId);
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
        requireActiveUser(userId);

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
                        .summaryMessage("아직 측정 기록이 없네요. 오늘의 건강 수치를 새로 기록해 보세요! 📝")
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
        String summaryMessage = generateSummaryMessage(metric.getMetricType(), values);

        return MetricQueryResponse.builder()
                .metricType(metricType)
                .summaryMessage(summaryMessage)
                .history(history)
                .build();
    }

    // 4. 나만의 건강지표 수동 생성
    @Transactional
    public UUID createCustomMetric(UUID userId, CreateMetricRequest request) {
        User user = userRepository.findByUserIdAndDeletedAtIsNull(userId)
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
    public MetricDetailResponse getMetricDetail(UUID userId, UUID valueId) {
        requireActiveUser(userId);

        HealthMetricValue v = valueRepository.findById(valueId)
                .orElseThrow(() -> new IllegalArgumentException("해당 기록을 찾을 수 없습니다."));

        HealthMetric m = v.getHealthMetric();
        if (!m.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 건강 기록만 조회할 수 있습니다.");
        }
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
        requireActiveUser(userId);

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
        requireActiveUser(userId);

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
                        User user = userRepository.findByUserIdAndDeletedAtIsNull(userId)
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

    private void requireActiveUser(UUID userId) {
        if (!userRepository.existsByUserIdAndDeletedAtIsNull(userId)) {
            throw new IllegalArgumentException("유저를 찾을 수 없습니다.");
        }
    }

    // 🔥 평균 계산 및 다정한 멘트 생성 메서드 (맨 아래쪽에 추가)
    private String generateSummaryMessage(String metricType, List<HealthMetricValue> values) {
        if (values == null || values.isEmpty()) {
            return "아직 측정 기록이 없네요. 오늘의 건강 수치를 새로 기록해 보세요! 📝";
        }

        int count = values.size();

        switch (metricType) {
            case TYPE_BLOOD_PRESSURE:
                double avgSys = values.stream().filter(v -> v.getSystolic() != null).mapToInt(HealthMetricValue::getSystolic).average().orElse(0);
                double avgDia = values.stream().filter(v -> v.getDiastolic() != null).mapToInt(HealthMetricValue::getDiastolic).average().orElse(0);

                if (avgSys == 0 && avgDia == 0) return "혈압 기록이 충분하지 않습니다.";

                String bpStatus;
                String bpAdvice;

                if (avgSys < 120 && avgDia < 80) {
                    bpStatus = "모두 정상 범위입니다";
                    bpAdvice = "안정적인 상태에요. 지금처럼 규칙적인 식습관과 가벼운 운동을 유지하세요 💚";
                } else if (avgSys < 140 && avgDia < 90) {
                    bpStatus = "주의가 필요한 범위입니다";
                    bpAdvice = "혈압이 다소 높은 편이에요. 짠 음식을 줄이고 가벼운 유산소 운동을 추천해요 🏃‍♀️";
                } else {
                    bpStatus = "관리가 필요한 범위입니다";
                    bpAdvice = "혈압이 높은 편이에요. 전문의와 상담하고 꾸준히 건강을 체크해 주세요 ⚠️";
                }
                return String.format("최근 %d회 측정 %s. 수축기 평균 %.0fmmHg, 이완기 평균 %.0fmmHg로 %s",
                        count, bpStatus, avgSys, avgDia, bpAdvice);

            case TYPE_BLOOD_SUGAR:
                double avgBefore = values.stream().filter(v -> v.getBeforeMeal() != null).mapToInt(HealthMetricValue::getBeforeMeal).average().orElse(0);
                double avgAfter = values.stream().filter(v -> v.getAfterMeal() != null).mapToInt(HealthMetricValue::getAfterMeal).average().orElse(0);
                return String.format("최근 %d회 측정 기준, 공복 평균 %.0fmg/dL, 식후 평균 %.0fmg/dL 입니다. 당 섭취를 조절하며 꾸준히 관리해 보아요 🥗", count, avgBefore, avgAfter);

            case TYPE_CHOLESTEROL:
                double avgChol = values.stream().filter(v -> v.getTotalChol() != null).mapToDouble(HealthMetricValue::getTotalChol).average().orElse(0);
                return String.format("최근 %d회 평균 총콜레스테롤은 %.1fmg/dL 입니다. 기름진 음식은 피하고 규칙적인 운동을 잊지 마세요! 🚴‍♂️", count, avgChol);

            case TYPE_WEIGHT:
                double avgWeight = values.stream().filter(v -> v.getValue() != null).mapToDouble(HealthMetricValue::getValue).average().orElse(0);
                return String.format("최근 %d회 평균 체중은 %.1fkg 입니다. 무리하지 말고 건강한 페이스로 목표를 향해 나아가요! 💪", count, avgWeight);

            case TYPE_HEART:
                double avgHeart = values.stream().filter(v -> v.getValue() != null).mapToDouble(HealthMetricValue::getValue).average().orElse(0);
                return String.format("최근 %d회 평균 심박수는 %.0fbpm 입니다. 무리한 활동보다는 마음의 안정을 취하며 스트레스를 관리해 보세요 🧘‍♀️", count, avgHeart);

            case TYPE_LIVER:
                double avgLiver = values.stream().filter(v -> v.getValue() != null).mapToDouble(HealthMetricValue::getValue).average().orElse(0);
                return String.format("최근 %d회 평균 간 수치는 %.1fIU/L 입니다. 오늘은 간도 쉴 수 있게 충분한 휴식과 금주를 권장해요 🛌", count, avgLiver);

            case TYPE_KIDNEY:
                double avgKidney = values.stream().filter(v -> v.getValue() != null).mapToDouble(HealthMetricValue::getValue).average().orElse(0);
                return String.format("최근 %d회 평균 신장 수치는 %.1fmg/dL 입니다. 노폐물 배출을 위해 수분을 충분히 섭취해 주세요 💧", count, avgKidney);

            default:
                double avgDefault = values.stream().filter(v -> v.getValue() != null).mapToDouble(HealthMetricValue::getValue).average().orElse(0);
                return String.format("최근 %d회 측정 평균값은 %.1f 입니다. 꾸준한 기록이 건강의 첫걸음입니다! ✨", count, avgDefault);
        }
    }
}
