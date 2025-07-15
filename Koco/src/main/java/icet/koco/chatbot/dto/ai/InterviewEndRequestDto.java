package icet.koco.chatbot.dto.ai;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InterviewEndRequestDto {
	private Long sessionId;

	private boolean finished;
}
