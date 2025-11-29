package com.todak.api.summary.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "summary")
public class Summary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "summary_id")
    private Long summaryId;

    @Column(name = "consultation_id")
    private Long consultationId;

    @Column(name = "recording_id")
    private Long recordingId;

    // 요약 텍스트
    @Column(columnDefinition = "TEXT")
    private String content;

    // 태그 (JSON 문자열 또는 콤마 문자열)
    @Column(columnDefinition = "TEXT")
    private String tags;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}
