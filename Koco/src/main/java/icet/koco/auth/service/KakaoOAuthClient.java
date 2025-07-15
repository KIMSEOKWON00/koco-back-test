package icet.koco.auth.service;

import icet.koco.auth.dto.KakaoTokenResponse;
import icet.koco.auth.dto.KakaoUserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.BodyInserters;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoOAuthClient {

    private final WebClient kakaoAuthClient; // 토큰 요청용
    private final WebClient kakaoApiClient;  // 사용자 API용

    @Value("${KAKAO_CLIENT_ID}")
    private String clientId;

    @Value("${KAKAO_CLIENT_SECRET}")
    private String clientSecret;

    @Value("${KAKAO_REDIRECT_URI}")
    private String redirectUri;

    public KakaoUserResponse getUserInfo(String code) {
        String token = kakaoAuthClient.post()
            .uri("/oauth/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                .with("client_id", clientId)
                .with("client_secret", clientSecret)
                .with("redirect_uri", redirectUri)
                .with("code", code)
            )
            .retrieve()
            .onStatus(status -> status.isError(), response ->
                response.bodyToMono(String.class)
                    .doOnNext(body -> System.out.println(">>> ❌ Kakao 토큰 요청 에러 응답: " + body))
                    .map(body -> new RuntimeException("카카오 토큰 요청 실패: " + body))
            )
            .bodyToMono(KakaoTokenResponse.class)
            .block()
            .getAccess_token();

        return kakaoApiClient.get()
            .uri("/v2/user/me")
            .header("Authorization", "Bearer " + token)
            .retrieve()
            .bodyToMono(KakaoUserResponse.class)
            .block();
    }

    public void unlinkUser(String kakaoUserId) {
        try {
            String response = kakaoApiClient.post()
                .uri("/v1/user/unlink")
                .body(BodyInserters.fromFormData("target_id_type", "user_id")
                    .with("target_id", kakaoUserId))
                .retrieve()
                .bodyToMono(String.class)
                .block();

            log.info(">>>>> Kakao 사용자 연결 끊기 성공: {}", response);
        } catch (Exception e) {
            log.warn(">>>>> Kakao unlink 실패: {}", e.getMessage());
        }
    }
}