package icet.koco.chatbot.repository;

import icet.koco.chatbot.entity.ChatSession;
import icet.koco.chatbot.entity.ChatSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatSummaryRepository extends JpaRepository<ChatSummary, Long> {
	Optional<ChatSummary> findByChatSession(ChatSession session);
}
