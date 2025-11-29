// src/main/java/com/todak/api/recording/service/RecordingService.java
package com.todak.api.recording.service;

import com.todak.api.recording.dto.response.RecordingDetailResponseDto;

public interface RecordingService {

    RecordingDetailResponseDto getRecording(Long id);

    void updateTranscript(Long id, String transcript);

    void updateStatus(Long id, String status);
}
