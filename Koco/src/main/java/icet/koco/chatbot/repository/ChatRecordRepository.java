package icet.koco.chatbot.repository;

import icet.koco.chatbot.entity.ChatRecord;
import icet.koco.chatbot.entity.ChatSession;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatRecordRepository extends JpaRepository<ChatRecord, Long> {

	@Query("SELECT c FROM ChatRecord c WHERE c.chatSession.id = :sessionId ORDER BY c.createdAt DESC")
	List<ChatRecord> findLast10BySessionId(@Param("sessionId") Long sessionId, Pageable pageable);

	default List<ChatRecord> findLast10BySessionId(Long sessionId) {
		return findLast10BySessionId(sessionId, PageRequest.of(0, 10));
	}

	// turn을 자동으로 다음값으로 설정하기 위해서
	@Query("SELECT COALESCE(MAX(r.turn), 0) FROM ChatRecord r WHERE r.chatSession.id = :sessionId")
	int findMaxTurnBySessionId(@Param("sessionId") Long sessionId);

	Long countByChatSession(ChatSession chatSession);

	// sessionId를 기준으로 채팅 이록 가져오기
	List<ChatRecord> findByChatSessionIdOrderByCreatedAtAsc(Long sessionId);
}
