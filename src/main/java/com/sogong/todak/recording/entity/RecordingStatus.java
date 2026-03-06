package com.sogong.todak.recording.entity;

public enum RecordingStatus {
    CREATED,     // 레코드만 생성됨(업로드 전)
    UPLOADED,    // S3 업로드 완료
    PROCESSING,  // STT/요약 진행 중
    DONE,        // 요약까지 완료
    FAILED       // 실패
}