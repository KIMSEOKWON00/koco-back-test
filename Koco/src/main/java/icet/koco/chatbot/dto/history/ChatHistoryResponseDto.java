package icet.koco.chatbot.dto.history;

import icet.koco.chatbot.entity.ChatRecord;
import icet.koco.chatbot.entity.ChatSession;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class ChatHistoryResponseDto {
	private String sessionId;
	private String title;
	private LocalDate date;
	private String type;
	private List<ChatRecordResponseDto> records;

	public static ChatHistoryResponseDto from(ChatSession session, List<ChatRecord> records) {
		return ChatHistoryResponseDto.builder()
			.sessionId(session.getId().toString())
			.title(session.getTitle())
			.date(session.getCreatedAt().toLocalDate())
			.type(session.getMode().name().toLowerCase()) // feedback 또는 interview
			.records(records.stream()
				.map(ChatRecordResponseDto::from)
				.collect(Collectors.toList()))
			.build();
	}
}
