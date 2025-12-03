package com.todak.api.recording.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RecordingUploadRequestDto {

    private Long consultationId;   // 업로드 대상 진료 ID
}
