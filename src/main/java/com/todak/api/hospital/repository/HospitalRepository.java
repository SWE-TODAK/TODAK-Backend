package com.todak.api.hospital.repository;

import com.todak.api.hospital.entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HospitalRepository extends JpaRepository<Hospital, Long> {

    List<Hospital> findByNameContainingIgnoreCase(String name);

    @Query("""
        SELECT h FROM Hospital h 
        JOIN h.departments d 
        WHERE d.name = :departmentName
    """)
    List<Hospital> findByDepartmentName(String departmentName);

    @Query("""
        SELECT h FROM Hospital h
        JOIN h.departments d
        WHERE LOWER(h.name) LIKE LOWER(CONCAT('%', :name, '%'))
        AND d.name = :departmentName
    """)
    List<Hospital> findByNameContainingIgnoreCaseAndDepartment(
            String name,
            String departmentName
    );
}
