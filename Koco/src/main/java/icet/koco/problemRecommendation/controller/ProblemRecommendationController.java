package icet.koco.problemRecommendation.controller;

import icet.koco.enums.ApiResponseCode;
import icet.koco.global.dto.ApiResponse;
import icet.koco.problemRecommendation.dto.RecommendationRequestDto;
import icet.koco.problemRecommendation.dto.RecommendedProblemResponseDto;
import icet.koco.problemRecommendation.service.ProblemRecommendationService;
import icet.koco.problemSet.dto.PreviousProblemListResponseDto;
import icet.koco.problemSet.service.ProblemService;
import io.swagger.v3.oas.annotations.Operation;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/backend/v1/problems")
public class ProblemRecommendationController {

	private final ProblemService problemService;
	private final ProblemRecommendationService problemRecommendationService;

	@Operation(summary = "전날 문제 리스트 정보를 가져오는 API입니다.")
	@GetMapping("/previous")
	public ResponseEntity<?> getPreviousProblemInfo() {

		PreviousProblemListResponseDto responseDto = problemService.getPreviousProblemInfo();

		return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.PREVIOUS_PROBLEM_FETCH_SUCCESS, "전날 문제 정보를 가져오는데 성공했습니다.", responseDto));
	}

	@Operation(summary = "AI 추천 문제 쌍 저장 API입니다.")
	@PostMapping("/recommend")
	public ResponseEntity<Void> saveRecommendations(@RequestBody RecommendationRequestDto dto) {
		problemRecommendationService.saveRecommendations(dto);
		return ResponseEntity.ok().build();
	}

	@Operation(summary = "AI 추천 문제 조회 API 입니다.")
	@GetMapping("/recommended")
	public ResponseEntity<?> getRecommendedProblems(
		@RequestParam("date") @DateTimeFormat(iso = ISO.DATE) LocalDate date) {
		Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		RecommendedProblemResponseDto responseDto = problemRecommendationService.getRecommendedProblems(userId, date);

		return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.RECOMMENDED_PROBLEM_FETCH_SUCCESS, "AI 추천 문제 조회에 성공했습니다.", responseDto));
	}
}
