// src/main/java/com/todak/api/recording/service/RecordingServiceImpl.java
package com.todak.api.recording.service;

import com.todak.api.recording.dto.response.RecordingDetailResponseDto;
import com.todak.api.recording.entity.Recording;
import com.todak.api.recording.entity.RecordingStatus;
import com.todak.api.recording.repository.RecordingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecordingServiceImpl implements RecordingService {

    private final RecordingRepository recordingRepository;

    @Override
    public RecordingDetailResponseDto getRecording(Long id) {
        Recording r = recordingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recording not found: " + id));
        return RecordingDetailResponseDto.from(r);
    }

    @Override
    public void updateTranscript(Long id, String transcript) {
        Recording r = recordingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recording not found: " + id));

        r.setTranscript(transcript);
        r.setStatus(RecordingStatus.TRANSCRIBED);

        recordingRepository.save(r);
    }

    @Override
    public void updateStatus(Long id, String status) {
        Recording r = recordingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recording not found: " + id));

        r.setStatus(RecordingStatus.valueOf(status));
        recordingRepository.save(r);
    }
}
