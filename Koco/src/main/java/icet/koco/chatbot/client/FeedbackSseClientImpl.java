package icet.koco.chatbot.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import icet.koco.chatbot.dto.ai.ChatbotFollowupRequestDto;
import icet.koco.chatbot.dto.ai.ChatbotStartRequestDto;
import icet.koco.chatbot.emitter.ChatEmitterRepository;
import icet.koco.chatbot.entity.ChatRecord.Role;
import icet.koco.chatbot.entity.ChatSession;
import icet.koco.chatbot.repository.ChatSessionRepository;
import icet.koco.chatbot.service.ChatRecordService;
import icet.koco.enums.ErrorMessage;
import icet.koco.global.exception.ResourceNotFoundException;
import java.io.IOException;
import java.time.Duration;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

@Component
@Primary
@Slf4j
@RequiredArgsConstructor
public class FeedbackSseClientImpl implements FeedbackSseClient {

	@Value("${AI_BASE_URL}")
	private String baseUrl;


	private final ChatEmitterRepository chatEmitterRepository;
	private final ChatSessionRepository chatSessionRepository;
	private final ChatRecordService chatRecordService;

	private WebClient webClient;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@PostConstruct
	public void initWebClient() {
		log.info("AI_BASE_URL: {}", baseUrl);

		this.webClient = WebClient.builder()
			.baseUrl(baseUrl)
			.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.build();
	}

	@Override
	public SseEmitter startFeedbackSession(ChatbotStartRequestDto requestDto) {
		SseEmitter emitter = new SseEmitter(0L); // 무제한 SSE
		chatEmitterRepository.save(requestDto.getSessionId(), emitter);

		// 요청 내용 로깅
		try {
			log.info(">>> [AI 요청] /api/ai/v2/feedback/start 전송 내용:\n{}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(requestDto));
		} catch (Exception e) {
			log.warn("requestDto 직렬화 실패", e);
		}


		StringBuilder fullResponse = new StringBuilder();

		webClient.post()
			.uri("/api/ai/v2/feedback/start")
			.bodyValue(requestDto)
			.accept(MediaType.TEXT_EVENT_STREAM)
			.retrieve()
			.bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {})
			.filter(event -> event.data() != null)
			.timeout(Duration.ofSeconds(60))
			.onErrorResume(e -> {
				emitter.completeWithError(e);
				return Flux.empty();
			})
			.doOnNext(event -> {
				String raw = (String) event.data(); // e.g. "data: Hello"

				String data = raw;

				if (data == null) {
					log.info("data is null");
					return;
				}

				if (data != null && data.startsWith("data: ")) {
					data = data.substring(6); // "data: " 제거
				}

				if (data != null && data.startsWith("[ERROR]")) {
					try {
						emitter.send(SseEmitter.event().name("error").data(data));
					} catch (IOException e) {
						emitter.completeWithError(e);
						return;
					}
					emitter.completeWithError(new RuntimeException(data));
				} else {
					try {
						emitter.send(SseEmitter.event().name("message").data(data));
						fullResponse.append(data); // 정제된 내용 저장
					} catch (IOException e) {
						emitter.completeWithError(e);
					}
				}
			})
			.doOnComplete(() -> {
				emitter.complete();
				ChatSession chatSession = chatSessionRepository.findByIdAndDeletedAtIsNull(requestDto.getSessionId())
					.orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.CHAT_SESSION_NOT_FOUND));
				chatRecordService.save(chatSession, Role.assistant, fullResponse.toString().trim());
			})
			.subscribe();

		return emitter;
	}

	@Override
	public SseEmitter streamFollowupFeedback(ChatbotFollowupRequestDto requestDto) {
		// SSE emitter 생성
		SseEmitter emitter = new SseEmitter(0L);
		chatEmitterRepository.save(requestDto.getSessionId(), emitter);

		// 요청 내용 로깅
		try {
			log.info(">>> [AI 요청] /api/ai/v2/feedback/answer 전송");
		} catch (Exception e) {
			log.warn("requestDto 직렬화 실패", e);
		}

		StringBuilder fullResponse = new StringBuilder();

		webClient.post()
			.uri("/api/ai/v2/feedback/answer")
			.bodyValue(requestDto)
			.accept(MediaType.TEXT_EVENT_STREAM)
			.retrieve()
			.bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {})
			.filter(event -> event.data() != null)
			.timeout(Duration.ofSeconds(60))
			.onErrorResume(e -> {
				emitter.completeWithError(e);
				return Flux.empty();
			})
			.doOnNext(event -> {
				String data = event.data();
//				log.info("AI 응답 수신: {}", data);

				if (data == null) {
					log.info("data is null");
					return;
				}

				String content = data.startsWith("data: ") ? data.substring(6) : data;

				try {
					if (content.startsWith("[ERROR]")) {
						emitter.send(SseEmitter.event().name("error").data(content));
						emitter.completeWithError(new RuntimeException(content));
					} else {
						emitter.send(SseEmitter.event().name("message").data(content));
						fullResponse.append(content);
					}
				} catch (IOException e) {
					emitter.completeWithError(e);
				}
			})
			.doOnComplete(() -> {
				emitter.complete();

				// ChatSession 조회
				ChatSession chatSession = chatSessionRepository.findByIdAndDeletedAtIsNull(requestDto.getSessionId())
					.orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.CHAT_SESSION_NOT_FOUND));

				// ChatRecord 저장
				chatRecordService.save(chatSession, Role.assistant, fullResponse.toString().trim());
			})
			.subscribe();

		return emitter;
	}

}
