package com.todak.api.infra.s3;

import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3UploaderService {

    private final S3Template s3Template;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * 파일 업로드 → key 반환
     */
    public String upload(MultipartFile file, String dirName) {
        try {
            String originalName = file.getOriginalFilename();
            String fileName = dirName + "/" + UUID.randomUUID() + "_" + originalName;

            s3Template.upload(bucket, fileName, file.getInputStream());
            return fileName;   // URL 말고 key 저장!
        } catch (Exception e) {
            throw new RuntimeException("S3 업로드 실패", e);
        }
    }

    /**
     * key 기반 다운로드 → MultipartFile 변환
     */
    public MultipartFile downloadAsMultipartFileByKey(String key) {
        try {
            InputStream inputStream = s3Template.download(bucket, key).getInputStream();

            return new MockMultipartFile(
                    key,
                    key,
                    "application/octet-stream",
                    inputStream
            );

        } catch (Exception e) {
            throw new RuntimeException("S3 다운로드 실패 (key 기반)", e);
        }
    }

    /**
     * ⚠️ 만약 URL 기반 다운로드도 유지하고 싶으면 아래 유지 가능
     */
    public MultipartFile downloadAsMultipartFile(String fileUrl) {
        try {
            String key = fileUrl.substring(fileUrl.indexOf(".com/") + 5);
            return downloadAsMultipartFileByKey(key);
        } catch (Exception e) {
            throw new RuntimeException("S3 다운로드 실패 (URL 기반)", e);
        }
    }
}
