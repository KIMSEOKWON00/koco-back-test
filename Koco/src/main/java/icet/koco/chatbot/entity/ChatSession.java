package icet.koco.chatbot.entity;


import icet.koco.user.entity.User;
import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Builder
@Table(name = "chat_session")
public class ChatSession {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	// 챗봇 모드
	@Enumerated(EnumType.STRING)
	@Column(name = "mode")
	private Mode mode;

	@Column(name = "problem_number")
	private Long problemNumber;

	@Column(name = "title")
	private String title;

	@Column(name = "user_code", columnDefinition = "LONGTEXT")
	private String userCode;

	@Column(name = "user_language")
	private String userLanguage;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	// 해당 세션이 종료되었는지 (AI 에이전트에서 판단한 값)
	@Builder.Default
	@Column(name = "is_finished", nullable = false)
	private Boolean finished = Boolean.FALSE;

	public enum Mode {
		feedback, interview;
	}
}
