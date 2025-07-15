package icet.koco.chatbot.dto.history;

import icet.koco.chatbot.entity.ChatRecord;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class ChatRecordResponseDto {
	private String role;
	private String content;
	private LocalDateTime createdAt;

	public static ChatRecordResponseDto from(ChatRecord record) {
		return ChatRecordResponseDto.builder()
			.role(record.getRole().name().toLowerCase()) // user 또는 assistant
			.content(record.getContent())
			.createdAt(record.getCreatedAt())
			.build();
	}
}
