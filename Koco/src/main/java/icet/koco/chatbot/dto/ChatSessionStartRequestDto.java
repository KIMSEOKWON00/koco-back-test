package icet.koco.chatbot.dto;

import icet.koco.chatbot.entity.ChatSession;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatSessionStartRequestDto {
	private Long problemNumber;
	private String language;
	private ChatSession.Mode mode; // "feedback" or "interview"
	private String userCode;
}
