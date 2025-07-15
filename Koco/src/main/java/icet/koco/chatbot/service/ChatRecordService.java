package icet.koco.chatbot.service;

import icet.koco.chatbot.entity.ChatRecord;
import icet.koco.chatbot.entity.ChatSession;
import icet.koco.chatbot.repository.ChatRecordRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import icet.koco.chatbot.entity.ChatRecord.Role;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatRecordService {
	private final ChatRecordRepository chatRecordRepository;

	public ChatRecord save(ChatSession session, Role role, String content) {
		// 현재 세션에서 가장 높은 turn 구해서 다음 값 계산
		int nextTurn = chatRecordRepository.findMaxTurnBySessionId(session.getId()) + 1;

		ChatRecord record = ChatRecord.builder()
			.chatSession(session)
			.turn(nextTurn)
			.role(role)
			.content(content)
			.createdAt(LocalDateTime.now())
			.build();

		return chatRecordRepository.save(record);
	}

}
