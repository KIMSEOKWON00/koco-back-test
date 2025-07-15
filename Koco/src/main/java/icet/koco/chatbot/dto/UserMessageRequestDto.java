package icet.koco.chatbot.dto;

import lombok.*;

@Data
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserMessageRequestDto {
	private String content;
}
