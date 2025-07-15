package icet.koco.chatbot.service;

import icet.koco.chatbot.dto.ai.InterviewEndRequestDto;
import icet.koco.chatbot.entity.ChatSession;
import icet.koco.chatbot.repository.ChatSessionRepository;
import icet.koco.enums.ErrorMessage;
import icet.koco.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InterviewService {
	private final ChatSessionRepository chatSessionRepository;

	public void interviewEndCheck(InterviewEndRequestDto requestDto) {
		ChatSession chatSession = chatSessionRepository.findByIdAndDeletedAtIsNull(requestDto.getSessionId())
			.orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.CHAT_SESSION_NOT_FOUND));

		boolean isEnded = requestDto.isFinished();
		System.out.println("해당 세션 종료되었는지 여부: " + isEnded);

		// 채팅 세션 상태 저장
		chatSession.setFinished(isEnded);
		chatSessionRepository.save(chatSession);
	}
}
