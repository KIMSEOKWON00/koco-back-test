package icet.koco.chatbot.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import icet.koco.chatbot.entity.ChatSession;
import icet.koco.chatbot.entity.QChatSession;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChatSessionRepositoryImpl implements ChatSessionRepositoryCustom{
	private final JPAQueryFactory queryFactory;

	@Override
	public List<ChatSession> findChatSessions(Long userId, Long cursorId, int size) {
		QChatSession session = QChatSession.chatSession;

		return queryFactory
			.selectFrom(session)
			.where(
				session.user.id.eq(userId),
				session.deletedAt.isNull(),
				cursorId != null ? session.id.lt(cursorId) : null
			)
			.orderBy(session.id.desc())
			.limit(size)
			.fetch();
	}
}
