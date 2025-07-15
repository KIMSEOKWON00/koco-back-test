package icet.koco.chatbot.client;

import icet.koco.chatbot.dto.summary.ChatSummaryRequestDto;

public interface SummaryClient {
	String requestSummary(ChatSummaryRequestDto summaryDto);
}
