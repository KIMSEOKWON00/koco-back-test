package icet.koco.chatbot.client;

import icet.koco.chatbot.dto.summary.ChatSummaryRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
//@Primary
@Slf4j
@RequiredArgsConstructor
public class MockSummaryClient implements SummaryClient {
	@Override
	public String requestSummary(ChatSummaryRequestDto requestDto) {
		log.info("🧪 [MockSummaryClient] 요약 요청 수신 - sessionId: {}", requestDto.getSessionId());

		String joinedMessages = requestDto.getMessages().stream()
			.map(m -> m.getRole() + ": " + m.getContent())
			.reduce("", (acc, line) -> acc + "\n" + line);

		log.info("📝 요약 대상 메시지들: {}", joinedMessages);

		// 간단한 mock 요약 결과
		String summary = "✅ MOCK 요약: 사용자의 코드 흐름을 요약했습니다.";
		log.info("📌 MOCK 요약 응답: {}", summary);

		return summary;
	}
}
