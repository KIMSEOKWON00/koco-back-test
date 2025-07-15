package icet.koco.chatbot.entity;


import icet.koco.chatbot.dto.ChatSessionStartRequestDto;
import icet.koco.chatbot.dto.ai.ChatbotFollowupRequestDto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Builder
@Table(name = "chat_record")
public class ChatRecord {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "session_id")
	private ChatSession chatSession;

	@Column(name = "turn")
	private Integer turn;

	@Enumerated(EnumType.STRING)
	@Column(name = "role")
	private Role role;

	@Lob
	@Column(name = "content", columnDefinition = "LONGTEXT")
	private String content;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	public enum Role {
		user, assistant
	}

	public static List<ChatRecord> fromStartDto(ChatSessionStartRequestDto dto, ChatSession session) {
		return List.of(ChatRecord.builder()
			.chatSession(session)
			.turn(1)
			.role(Role.user)
			.content(dto.getUserCode())
			.createdAt(LocalDateTime.now())
			.build());
	}


	public static List<ChatRecord> fromAnswerDto(ChatbotFollowupRequestDto dto, ChatSession session) {
		List<ChatRecord> records = new ArrayList<>();
		int turn = 1;
		for (ChatbotFollowupRequestDto.Message m : dto.getMessages()) {
			records.add(ChatRecord.builder()
				.chatSession(session)
				.turn(turn++)
				.role(Role.valueOf(m.getRole()))  // "user" 또는 "assistant"
				.content(m.getContent())
				.createdAt(LocalDateTime.now())
				.build());
		}
		return records;
	}

}
