package icet.koco.chatbot.repository;

import icet.koco.chatbot.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long>, ChatSessionRepositoryCustom {
	// sessionId로 세션 찾기
	Optional<ChatSession> findByIdAndDeletedAtIsNull(Long id);

	List<ChatSession> findAllByIdInAndDeletedAtIsNull(List<Long> ids);

}
