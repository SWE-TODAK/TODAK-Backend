package com.sogong.todak.summary.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "summaries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Summary {

    @Id
    @Column(name = "summary_id", nullable = false)
    private UUID summaryId;

    @Column(name = "recording_id", nullable = false)
    private UUID recordingId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "intro", nullable = false, columnDefinition = "TEXT")
    private String intro;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public static Summary create(
            UUID recordingId,
            String intro,
            String content
    ) {
        return Summary.builder()
                .summaryId(UUID.randomUUID())
                .recordingId(recordingId)
                .intro(intro)
                .content(content)
                .createdAt(OffsetDateTime.now())
                .build();
    }
}