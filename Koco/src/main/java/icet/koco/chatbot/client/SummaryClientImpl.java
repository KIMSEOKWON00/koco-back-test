package icet.koco.chatbot.client;

import icet.koco.chatbot.dto.summary.ChatSummaryRequestDto;
import icet.koco.chatbot.dto.summary.ChatSummaryResponseDto;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Slf4j
@Primary
@Component
@RequiredArgsConstructor
public class SummaryClientImpl implements SummaryClient {

	@Value("${AI_BASE_URL}")
	private String baseUrl;

	private WebClient webClient;

	@PostConstruct
	public void initWebClient() {
		this.webClient = WebClient.builder()
			.baseUrl(baseUrl)
			.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.build();
	}

	@Override
	public String requestSummary(ChatSummaryRequestDto requestDto) {
		try {
			List<ChatSummaryRequestDto> list = List.of(requestDto);
			ChatSummaryResponseDto[] response = webClient.post()
				.uri("/api/ai/v2/summary")
				.bodyValue(list)
				.retrieve()
				.bodyToMono(ChatSummaryResponseDto[].class)
				.block();

			if (response != null && response.length > 0) {
				log.info("요약 응답 수신 완료 - sessionId: {}", response[0].getSessionId());
				return response[0].getSummary();
			} else {
				log.warn("요약 응답이 비어 있음");
				return "";
			}
		} catch (Exception e) {
			log.error("요약 요청 중 오류 발생", e);
			return "";
		}
	}
}
