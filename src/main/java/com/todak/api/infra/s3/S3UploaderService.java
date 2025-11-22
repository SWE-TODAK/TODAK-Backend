package com.todak.api.infra.s3; // infra 패키지 안의 s3 패키지

import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3UploaderService {

    private final S3Template s3Template;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    public String upload(MultipartFile file, String dirName) throws IOException {
        String originalName = file.getOriginalFilename();
        String fileName = dirName + "/" + UUID.randomUUID() + "_" + originalName;

        s3Template.upload(bucket, fileName, file.getInputStream());

        return s3Template.download(bucket, fileName).getURL().toString();
    }
}