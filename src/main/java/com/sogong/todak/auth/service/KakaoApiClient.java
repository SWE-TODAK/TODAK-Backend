package com.sogong.todak.auth.service;

import com.sogong.todak.common.exception.ExternalApiException;
import com.sogong.todak.common.exception.KakaoUnlinkFailedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
public class KakaoApiClient implements KakaoUnlinkService {

    private final RestClient restClient = RestClient.builder().build();

    @Value("${app.kakao.admin-key:}")
    private String adminKey;

    @Value("${app.kakao.unlink-uri}")
    private String unlinkUri;

    @Override
    public void unlink(String providerUserId) {
        if (!StringUtils.hasText(adminKey)) {
            throw new ExternalApiException("카카오 unlink 설정이 올바르지 않습니다.");
        }

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("target_id_type", "user_id");
        body.add("target_id", providerUserId);

        try {
            restClient.post()
                    .uri(unlinkUri)
                    .header(HttpHeaders.AUTHORIZATION, "KakaoAK " + adminKey)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Kakao unlink completed.");
        } catch (RestClientResponseException ex) {
            log.warn("Kakao unlink failed with status={}", ex.getStatusCode().value());
            throw new KakaoUnlinkFailedException("카카오 연결 해제에 실패했습니다.", ex);
        } catch (RestClientException ex) {
            log.error("Kakao unlink call error.", ex);
            throw new ExternalApiException("카카오 unlink 호출 중 오류가 발생했습니다.", ex);
        }
    }
}
