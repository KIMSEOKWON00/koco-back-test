package icet.koco.chatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@Data
@Builder
@RequiredArgsConstructor
public class ChatSessionResponseDto {
	Long sessionId;
}
