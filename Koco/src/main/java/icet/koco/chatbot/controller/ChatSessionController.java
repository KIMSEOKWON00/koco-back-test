package icet.koco.chatbot.controller;

import icet.koco.chatbot.dto.ChatSessionResponseDto;
import icet.koco.chatbot.dto.ChatSessionStartRequestDto;
import icet.koco.chatbot.dto.UserMessageRequestDto;
import icet.koco.chatbot.dto.history.ChatHistoryResponseDto;
import icet.koco.chatbot.dto.history.ChatSessionListResponseDto;
import icet.koco.chatbot.entity.ChatSession;
import icet.koco.chatbot.repository.ChatSessionRepository;
import icet.koco.chatbot.service.ChatSessionService;
import icet.koco.enums.ApiResponseCode;
import icet.koco.enums.ErrorMessage;
import icet.koco.global.dto.ApiResponse;
import icet.koco.global.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/backend/v2/chat")
@Tag(name = "ChatBot", description = "챗봇 관련 API입니다.")
@RequiredArgsConstructor
public class ChatSessionController {

	private final ChatSessionService chatSessionService;
	private final ChatSessionRepository chatSessionRepository;

	@PostMapping("/init")
	@Operation(summary = "세션을 생성해서 sessionId를 리턴하는 API입니다.")
	public ResponseEntity<?> initChatSession(@RequestBody ChatSessionStartRequestDto requestDto) {
		Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		ChatSessionResponseDto responseDto = chatSessionService.initChatSession(userId, requestDto);
		return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.CHAT_SESSION_CREATED, "체팅 세션 생성", responseDto));
	}

	@PostMapping(value = "/session/{sessionId}/start", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	@Operation(summary = "챗봇을 호출하여 채팅을 시작하는 API입니다.")
	public SseEmitter startChatSession(@PathVariable Long sessionId) {
		Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String mode = chatSessionService.getModeBySessionId(sessionId);

		switch (mode) {
			case "feedback":
				return chatSessionService.startFeedbackSession(userId, sessionId);
			case "interview":
				return chatSessionService.startInterviewSession(userId, sessionId);
			default:
				throw new IllegalArgumentException("Unknown mode: " + mode);
		}
	}

	@PostMapping(value = "/session/{sessionId}/followup", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	@Operation(summary = "후속질문하는 API입니다.")
	public SseEmitter sendMessage(@PathVariable Long sessionId,
											@RequestBody UserMessageRequestDto requestDto) {
		Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		String mode = getMode(sessionId).toString();

		if (mode.equals("feedback")) {
			return chatSessionService.followupFeedbackSession(sessionId, userId, requestDto.getContent());
		} else if (mode.equals("interview")) {
			return chatSessionService.followupInterviewSession(sessionId, userId, requestDto.getContent());
		}
		return null;
	}

	/**
	 * 세션 삭제 API
	 * @param sessionIdsHeader RequestHeader의 sessionId
	 * @return	204
	 */
	@DeleteMapping("/session/delete")
	@Operation(summary = "선택한 챗봇 세션을 삭제하는 API입니다.")
	public ResponseEntity<?> deleteChatSession( @RequestHeader("Session-Ids") String sessionIdsHeader) {
		Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		System.out.println(sessionIdsHeader);

		if (sessionIdsHeader == null || sessionIdsHeader.trim().isEmpty()) {
			throw new IllegalArgumentException("Session-Ids 헤더가 비어있습니다.");
		}

		List<Long> sessionIds = Arrays.stream(sessionIdsHeader.split(", "))
			.map(String::trim)
			.map(Long::parseLong)
			.collect(Collectors.toList());

		System.out.println("sessionIds: " + sessionIds);

		chatSessionService.deleteChatSessions(userId, sessionIds);

		return ResponseEntity.noContent().build();
	}

	/**
	 * 대화 이력 세부 조회
	 * @param sessionId pathVariable
	 * @return 대화 이력
	 */
	@Operation(summary = "챗봇 세션 대화 이력 세부 조회를 하는 API입니다.")
	@GetMapping("/history/{sessionId}")
	public ResponseEntity<?> getChatSessionHistory(@PathVariable Long sessionId) {
		Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		ChatHistoryResponseDto responseDto = chatSessionService.getChatSessionHistory(userId, sessionId);

		return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.CHAT_HISTORY_FETCH_SUCCESS, "대화 이력을 성공적으로 불러왔습니다.", responseDto));
	}

	@Operation(summary = "챗봇 세션 리스트를 조회하는 API입니다.")
	@GetMapping("/history")
	public ResponseEntity<?> getChatSessionList(
		@RequestParam(required = false) Long cursorId,
		@RequestParam(required = false, defaultValue = "10") int size
	) {
		Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		ChatSessionListResponseDto responseDto = chatSessionService.getChatSessionList(userId, cursorId, size);

		return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.CHAT_LIST_FETCH_SUCCESS, "대화 세션 리스트 조회에 성공하였습니다.", responseDto));
	}

	/**
	 * 세션의 모드를 구함
	 * @param sessionId 세션 ID
	 * @return ChatSession.Mode
	 */
	public String getMode(Long sessionId) {
		ChatSession chatSession = chatSessionRepository.findByIdAndDeletedAtIsNull(sessionId)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.CHAT_SESSION_NOT_FOUND));

		return chatSession.getMode().toString();
	}
}
