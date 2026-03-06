package com.sogong.todak.recording.service;

import org.springframework.stereotype.Service;

@Service
public class S3PresignService {

    public String presignPutUrl(String key, String mimeType) {
        // 일단 컴파일 통과용 더미
        return "https://dummy-presigned-url.com/" + key;
    }
}