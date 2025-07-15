package icet.koco.chatbot.service;

import icet.koco.chatbot.client.FeedbackSseClient;
import icet.koco.chatbot.client.InterviewSseClient;
import icet.koco.chatbot.client.SummaryClient;
import icet.koco.chatbot.dto.ChatSessionResponseDto;
import icet.koco.chatbot.dto.ChatSessionStartRequestDto;
import icet.koco.chatbot.dto.ai.ChatbotFollowupRequestDto;
import icet.koco.chatbot.dto.ai.ChatbotStartRequestDto;
import icet.koco.chatbot.dto.history.ChatHistoryResponseDto;
import icet.koco.chatbot.dto.history.ChatSessionListResponseDto;
import icet.koco.chatbot.dto.summary.ChatSummaryRequestDto;
import icet.koco.chatbot.entity.ChatRecord;
import icet.koco.chatbot.entity.ChatSession;
import icet.koco.chatbot.entity.ChatSummary;
import icet.koco.chatbot.repository.ChatRecordRepository;
import icet.koco.chatbot.repository.ChatSessionRepository;
import icet.koco.chatbot.repository.ChatSummaryRepository;
import icet.koco.enums.ErrorMessage;
import icet.koco.global.exception.ForbiddenException;
import icet.koco.global.exception.InterviewAlreadyFinishedException;
import icet.koco.global.exception.ResourceNotFoundException;
import icet.koco.problemSet.entity.Problem;
import icet.koco.problemSet.repository.ProblemRepository;
import icet.koco.user.entity.User;
import icet.koco.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatSessionService {

	private final ChatRecordService chatRecordService;

	private final ChatSessionRepository chatSessionRepository;
	private final ProblemRepository problemRepository;
	private final UserRepository userRepository;
	private final ChatRecordRepository chatRecordRepository;
	private final ChatSummaryRepository chatSummaryRepository;

	private final FeedbackSseClient feedbackSseClient;
	private final InterviewSseClient interviewSseClient;
	private final SummaryClient summaryClient;

	public ChatSessionResponseDto initChatSession(Long userId, ChatSessionStartRequestDto requestDto) {
		// 사용자 찾기
		User user = userRepository.findByIdAndDeletedAtIsNull((userId))
				.orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

		// 문제 찾기
		Problem problem = problemRepository.findByNumber(requestDto.getProblemNumber())
				.orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.PROBLEM_NOT_FOUND));

		// 채팅 세션 생성
		ChatSession chatSession = chatSessionRepository.save(
				ChatSession.builder()
						.user(user)
						.mode(requestDto.getMode())
						.problemNumber(problem.getNumber())
						.title(problem.getTitle())
						.userCode(requestDto.getUserCode())
						.userLanguage(requestDto.getLanguage())
						.createdAt(LocalDateTime.now())
						.finished(false)
						.build()
		);

		// 채팅 세션 Id
		Long sessionId = chatSession.getId();

		// 최초 user 메시지 저장
		List<ChatRecord> initRecord = ChatRecord.fromStartDto(requestDto, chatSession);
		chatRecordRepository.saveAll(initRecord);

		// sessionId 리턴
		return ChatSessionResponseDto.builder()
				.sessionId(sessionId)
				.build();
	}

	public SseEmitter startFeedbackSession(Long userId, Long sessionId) {
		// 사용자 확인
		userRepository.findByIdAndDeletedAtIsNull(userId)
				.orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

		// 세션 찾기
		ChatSession chatSession = chatSessionRepository.findByIdAndDeletedAtIsNull(sessionId)
				.orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.CHAT_SESSION_NOT_FOUND));

		// 문제 찾기
		Problem problem = problemRepository.findByNumber(chatSession.getProblemNumber())
				.orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.PROBLEM_NOT_FOUND));

		// AI 피드백 요청
		ChatbotStartRequestDto request = ChatbotStartRequestDto.builder()
				.sessionId(chatSession.getId())
				.problemNumber(problem.getNumber())
				.title(problem.getTitle())
				.description(problem.getDescription())
				.inputDescription(problem.getInputDescription())
				.outputDescription(problem.getOutputDescription())
				.inputExample(problem.getInputExample())
				.outputExample(problem.getOutputExample())
				.codeLanguage(chatSession.getUserLanguage())
				.code(chatSession.getUserCode())
				.build();

		return feedbackSseClient.startFeedbackSession(request);
	}

	/**
	 * 피드백 세션 후속 답변
	 * @param sessionId
	 * @param userId
	 * @param content
	 * @return
	 */
	public SseEmitter followupFeedbackSession (Long sessionId, Long userId, String content) {
		// 사용자 있는지 확인
		userRepository.findByIdAndDeletedAtIsNull(userId)
				.orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

		// 채팅 세션 찾기
		ChatSession session = chatSessionRepository.findByIdAndDeletedAtIsNull(sessionId)
				.orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.CHAT_SESSION_NOT_FOUND));

		// 유저가 보낸 메시지 저장
		chatRecordService.save(session, ChatRecord.Role.user, content);

		// 전체 메시지 수 체크 (해당 세션 기준)
		long totalCount = chatRecordRepository.countByChatSession(session);

		String summary;
		boolean shouldUpdateSummary = (totalCount == 3 || totalCount % 11 == 0);

		if (shouldUpdateSummary) {
			// 새로운 summary 요청
			List<ChatRecord> latestMessages = chatRecordRepository.findLast10BySessionId(sessionId);
			ChatSummaryRequestDto summaryDto = ChatSummaryRequestDto.from(sessionId, latestMessages);

			summary = summaryClient.requestSummary(summaryDto);
			saveOrUpdateSummary(session, summary);
		} else {
			// 기존 summary 가져오기
			summary = chatSummaryRepository.findByChatSession(session)
					.map(ChatSummary::getSummary)
					.orElse("");
		}

		// 후속 피드백 요청
		List<ChatRecord> latestMessages = chatRecordRepository.findLast10BySessionId(sessionId);
		ChatbotFollowupRequestDto answerRequest = ChatbotFollowupRequestDto.from(sessionId, summary, latestMessages);
		return feedbackSseClient.streamFollowupFeedback(answerRequest);
	}

	public SseEmitter startInterviewSession(Long userId, Long sessionId) {
		// 사용자 확인
		userRepository.findByIdAndDeletedAtIsNull(userId)
				.orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

		// 세션 찾기
		ChatSession chatSession = chatSessionRepository.findByIdAndDeletedAtIsNull(sessionId)
				.orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.CHAT_SESSION_NOT_FOUND));

		// 문제 찾기
		Problem problem = problemRepository.findByNumber(chatSession.getProblemNumber())
				.orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.PROBLEM_NOT_FOUND));

		// AI 피드백 요청
		ChatbotStartRequestDto request = ChatbotStartRequestDto.builder()
				.sessionId(chatSession.getId())
				.problemNumber(problem.getNumber())
				.title(problem.getTitle())
				.description(problem.getDescription())
				.inputDescription(problem.getInputDescription())
				.outputDescription(problem.getOutputDescription())
				.inputExample(problem.getInputExample())
				.outputExample(problem.getOutputExample())
				.codeLanguage(chatSession.getUserLanguage())
				.code(chatSession.getUserCode())
				.build();

		return interviewSseClient.startInterviewSession(request);
	}

	/**
	 * 인터뷰 챗봇 후속 답변
	 * @param sessionId
	 * @param userId
	 * @param content
	 * @return
	 */
	public SseEmitter followupInterviewSession(Long sessionId, Long userId, String content) {
		// 사용자 있는지 확인
		userRepository.findByIdAndDeletedAtIsNull(userId)
				.orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

		// 채팅 세션 찾기
		ChatSession chatSession = chatSessionRepository.findByIdAndDeletedAtIsNull(sessionId)
				.orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.CHAT_SESSION_NOT_FOUND));

		// 종료된 세션이면 예외
		if (chatSession.getFinished()) {
			throw new InterviewAlreadyFinishedException(ErrorMessage.INTERVIEW_ALREADY_FINISHED);
		}

		// 유저가 보낸 메세지 저장
		chatRecordService.save(chatSession, ChatRecord.Role.user, content);

		// 전체 메세지 수 체크
		Long totalCount = chatRecordRepository.countByChatSession(chatSession);

		// 요약된 메세지
		String summary;

		boolean shouldUpdateSummary = (totalCount == 3 || totalCount % 11 == 0);

		if (shouldUpdateSummary) {
			List<ChatRecord> latestMessages = chatRecordRepository.findLast10BySessionId(sessionId);
			ChatSummaryRequestDto summaryDto = ChatSummaryRequestDto.from(sessionId, latestMessages);

			summary = summaryClient.requestSummary(summaryDto);
		} else {
			// 기존 summary 사용
			summary = chatSummaryRepository.findByChatSession(chatSession)
					.map(ChatSummary::getSummary)
					.orElse("");
		}

		// 후속 인터뷰 AI 요청
		List<ChatRecord> latestMessages = chatRecordRepository.findLast10BySessionId(sessionId);
		ChatbotFollowupRequestDto answerRequest = ChatbotFollowupRequestDto.from(sessionId, summary, latestMessages);
		return interviewSseClient.streamFollowupInterview(answerRequest);
	}

	/**
	 * 세션 채팅 이력 세부 조회
	 * @param userId
	 * @param sessionId
	 * @return
	 */
	public ChatHistoryResponseDto getChatSessionHistory(Long userId, Long sessionId) {
		// 사용자 찾기
		User user = userRepository.findByIdAndDeletedAtIsNull(userId)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

		// 세션 찾기
		ChatSession chatSession = chatSessionRepository.findByIdAndDeletedAtIsNull(sessionId)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.CHAT_SESSION_NOT_FOUND));

		// 권한 확인
		if (!chatSession.getUser().equals(user)) {
			throw new ForbiddenException(ErrorMessage.NO_CHAT_HISTORY_PERMISSION);
		}

		// DTO 생성
		List<ChatRecord> chatRecords = chatRecordRepository.findByChatSessionIdOrderByCreatedAtAsc(sessionId);

		return ChatHistoryResponseDto.from(chatSession, chatRecords);

	}

	/**
	 * 챗봇 세션 리스트 조회
	 * @param userId 사용자 ID
	 * @param cursorId 커서
	 * @param size 사이즈 (기본 10)
	 * @return ChatSessionListResponseDto
	 */
	public ChatSessionListResponseDto getChatSessionList(Long userId, Long cursorId, int size) {
		// 유저 찾기
		userRepository.findByIdAndDeletedAtIsNull(userId)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

		List<ChatSession> sessions = chatSessionRepository.findChatSessions(userId, cursorId,
			size + 1);

		boolean hasNext = sessions.size() > size;
		if (hasNext)
			sessions.remove(size);

		Long nextCursorId = sessions.isEmpty() ? null : sessions.get(sessions.size() - 1).getId();

		List<ChatSessionListResponseDto.ChatSessionDto> sessionDtos = sessions.stream()
			.map(session -> ChatSessionListResponseDto.ChatSessionDto.builder()
				.sessionId(session.getId())
				.title(session.getTitle())
				.mode(session.getMode())
				.createdAt(session.getCreatedAt())
				.build())
			.toList();

		return ChatSessionListResponseDto.builder()
			.nextCursorId(nextCursorId)
			.hasNext(hasNext)
			.chatSessions(sessionDtos)
			.build();
	}

	/**
	 * 요약 저장 / 업데이트
	 * @param session
	 * @param summary
	 */
	private void saveOrUpdateSummary(ChatSession session, String summary) {
		Optional<ChatSummary> optional = chatSummaryRepository.findByChatSession(session);
		if (optional.isPresent()) {
			ChatSummary existing = optional.get();
			existing.setSummary(summary);
			existing.setCreatedAt(LocalDateTime.now());
			chatSummaryRepository.save(existing);
		} else {
			ChatSummary newOne = ChatSummary.builder()
					.chatSession(session)
					.summary(summary)
					.createdAt(LocalDateTime.now())
					.build();
			chatSummaryRepository.save(newOne);
		}
	}

	public void deleteChatSessions(Long userId, List<Long> sessionIds) {
		// 사용자 확인
		userRepository.findByIdAndDeletedAtIsNull(userId)
				.orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

		// 세션들 찾기
		List<ChatSession> chatSessions = chatSessionRepository.findAllByIdInAndDeletedAtIsNull(sessionIds);

		// 비어있는지 확인
		if (sessionIds == null || sessionIds.isEmpty()) {
			throw new IllegalArgumentException("삭제할 세션 ID가 비어 있습니다.");
		}

		for (ChatSession chatSession : chatSessions) {
			if (!chatSession.getUser().getId().equals(userId)) {
				throw new IllegalArgumentException("본인 세션만 삭제할 수 있습니다.");
			}
			chatSession.setDeletedAt(LocalDateTime.now()); // softDelete 처리
			chatSessionRepository.save(chatSession);
		}

	}

	public String getModeBySessionId(Long sessionId) {
		return chatSessionRepository.findById(sessionId)
				.orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.CHAT_SESSION_NOT_FOUND))
				.getMode().toString();
	}

}
