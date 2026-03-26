package com.sogong.todak.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class AwsS3Config {

    @Bean
    public S3Presigner s3Presigner(@Value("${aws.s3.region}") String region) {
        return S3Presigner.builder()
                .region(Region.of(region))
                // 특정 프로필을 찾는 대신, 시스템 환경에 맞는 자격 증명을 자동으로 찾도록 변경
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
