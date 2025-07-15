package icet.koco.chatbot.client;

import icet.koco.chatbot.dto.ai.ChatbotFollowupRequestDto;
import icet.koco.chatbot.dto.ai.ChatbotStartRequestDto;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface FeedbackSseClient {
	SseEmitter startFeedbackSession(ChatbotStartRequestDto requestDto);
	SseEmitter streamFollowupFeedback(ChatbotFollowupRequestDto requestDto);
}
