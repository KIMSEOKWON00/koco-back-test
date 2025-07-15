package icet.koco.chatbot.repository;

import icet.koco.chatbot.entity.ChatSession;
import java.util.List;

public interface ChatSessionRepositoryCustom {
	List<ChatSession> findChatSessions(Long userId, Long cursorId, int limit);
}
