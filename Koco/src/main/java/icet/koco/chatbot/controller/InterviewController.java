package icet.koco.chatbot.controller;

import icet.koco.chatbot.dto.ai.InterviewEndRequestDto;
import icet.koco.chatbot.service.InterviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Interview", description = "AI서버로부터 종료 여부 수신")
@RequestMapping("/api/backend/v2/chat/interview/end")
@RequiredArgsConstructor
@RestController
public class InterviewController {
	private final InterviewService interviewService;

	@Operation(summary = "AI서버로부터 인터뷰 종료 여부 저장")
	@PostMapping
	public ResponseEntity<?> interviewEndCheck(@RequestBody InterviewEndRequestDto requestDto) {
		interviewService.interviewEndCheck(requestDto);
		return ResponseEntity.noContent().build();
	}
}
