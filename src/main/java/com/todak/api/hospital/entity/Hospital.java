package com.todak.api.hospital.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "hospitals")
public class Hospital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long hospitalId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 200)
    private String address;

    @Column(length = 20)
    private String phone;

    @Column(columnDefinition = "jsonb")
    private String openHours;   // JSONB 그대로 저장

    @Column(length = 16)
    private String hospitalAuthKey;

    @Column(length = 500)
    private String introduction;

    @CreationTimestamp
    private OffsetDateTime createdAt;

    // ⭐ 병원 - 진료과 관계 (ManyToMany)
    @ManyToMany
    @JoinTable(
            name = "hospital_departments",
            joinColumns = @JoinColumn(name = "hospital_id"),
            inverseJoinColumns = @JoinColumn(name = "department_id")
    )
    private List<Department> departments;

    @OneToMany(mappedBy = "hospital", cascade = CascadeType.ALL)
    private List<Doctor> doctors = new ArrayList<>();
}
