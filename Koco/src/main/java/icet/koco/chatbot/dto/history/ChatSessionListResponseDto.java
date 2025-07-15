package icet.koco.chatbot.dto.history;

import icet.koco.chatbot.entity.ChatSession;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class ChatSessionListResponseDto {
	private Long nextCursorId;
	private boolean hasNext;
	private List<ChatSessionDto> chatSessions;

	@Getter
	@Builder
	public static class ChatSessionDto {
		private Long sessionId;
		private String title;
		private ChatSession.Mode mode;
		private LocalDateTime createdAt;
	}
}
