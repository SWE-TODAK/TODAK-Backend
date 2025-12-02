package com.todak.api.hospital.service;

import com.todak.api.hospital.dto.response.*;
import com.todak.api.hospital.entity.Department;
import com.todak.api.hospital.entity.Doctor;
import com.todak.api.hospital.entity.Hospital;
import com.todak.api.hospital.repository.HospitalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class HospitalServiceImpl implements HospitalService {

    private final HospitalRepository hospitalRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<HospitalListResponseDto> searchHospitals(String search, String department) {

        List<Hospital> hospitals;

        if (search != null && department != null) {
            hospitals = hospitalRepository.findByNameContainingIgnoreCaseAndDepartment(search, department);
        } else if (search != null) {
            hospitals = hospitalRepository.findByNameContainingIgnoreCase(search);
        } else if (department != null) {
            hospitals = hospitalRepository.findByDepartmentName(department);
        } else {
            hospitals = hospitalRepository.findAll();
        }

        return hospitals.stream()
                .map(this::toListDto)
                .toList();
    }

    @Override
    public HospitalDetailResponseDto getHospitalDetail(Long id) {

        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Hospital not found: " + id));

        return toDetailDto(hospital);
    }

    @Override
    public void toggleFavorite(Long hospitalId, String userId) {
        /**
         * 찜 저장 로직은 DB 구조에 따라 다름.
         * - favorite 테이블이 있다면 INSERT/DELETE
         * - hospital 테이블에 favorite_count만 있다면 count 변경
         *
         * 지금은 구조가 없으므로 TODO로 둠.
         */
        System.out.println("찜 상태 변경됨. hospitalId=" + hospitalId + ", userId=" + userId);
    }

    /* --------------------------------------------
       DTO 변환 메서드들 (Mapper 역할)
       -------------------------------------------- */

    private HospitalListResponseDto toListDto(Hospital hospital) {

        List<String> categories = hospital.getDepartments().stream()
                .map(Department::getName)
                .toList();

        return HospitalListResponseDto.builder()
                .hospitalId(hospital.getHospitalId())
                .name(hospital.getName())
                .address(hospital.getAddress())
                .categories(categories)
                .favorite(false) // TODO: userId가 있으면 계산해서 넣으면 됨
                .build();
    }

    private HospitalDetailResponseDto toDetailDto(Hospital hospital) {

        // 1. 병원 진료과 목록
        List<DepartmentDto> departmentDtos = hospital.getDepartments().stream()
                .map(dep -> DepartmentDto.builder()
                        .departmentId(dep.getDepartmentId())
                        .name(dep.getName())
                        .build())
                .toList();

        // 2. 소속 의사 목록
        List<DoctorDto> doctorDtos = hospital.getDoctors().stream()
                .map(this::toDoctorDto)
                .toList();

        // 3. 운영시간 JSONB → DTO
        AvailableHoursDto hoursDto = toAvailableHoursDto(hospital.getOpenHours());

        return HospitalDetailResponseDto.builder()
                .hospitalId(hospital.getHospitalId())
                .name(hospital.getName())
                .address(hospital.getAddress())
                .phone(hospital.getPhone())
                .introduction(hospital.getIntroduction())
                .favorite(false)
                .doctors(doctorDtos)
                .availableHours(hoursDto)
                .build();
    }

    private DoctorDto toDoctorDto(Doctor doctor) {
        return DoctorDto.builder()
                .doctorId(doctor.getDoctorId())
                .name(doctor.getName())
                .specialty(doctor.getSpecialty())
                .mainDepartmentId(doctor.getMainDepartment().getDepartmentId())
                .build();
    }

    private AvailableHoursDto toAvailableHoursDto(Object jsonbObj) {

        if (jsonbObj == null)
            return null;

        try {
            // 1. JSONB가 문자열로 들어오므로 그대로 String으로 변환
            String jsonStr = jsonbObj.toString();

            // 2. JSON 문자열을 Map으로 변환
            Map<String, String> map = objectMapper.readValue(
                    jsonStr, new TypeReference<Map<String, String>>() {
                    }
            );

            return AvailableHoursDto.builder()
                    .mon(map.get("mon"))
                    .tue(map.get("tue"))
                    .wed(map.get("wed"))
                    .thu(map.get("thu"))
                    .fri(map.get("fri"))
                    .sat(map.get("sat"))
                    .sun(map.get("sun"))
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse open_hours JSON", e);
        }
    }
}