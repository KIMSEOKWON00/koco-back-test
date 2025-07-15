package icet.koco.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Profile("test") // test 환경에서만 활성화
@Configuration
public class TestWebClientConfig {

	@Bean
	public WebClient kakaoAuthClient() {
		return WebClient.builder()
			.baseUrl("https://dummy.kakao.auth") // 더미 URL
			.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
			.build();
	}

	@Bean
	public WebClient kakaoApiClient() {
		return WebClient.builder()
			.baseUrl("https://dummy.kakao.api") // 더미 URL
			.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
			.defaultHeader(HttpHeaders.AUTHORIZATION, "KakaoAK dummy-key")
			.build();
	}
}
