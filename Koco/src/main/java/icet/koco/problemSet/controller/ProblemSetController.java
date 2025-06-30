package icet.koco.problemSet.controller;

import icet.koco.enums.ApiResponseCode;
import icet.koco.global.dto.ApiResponse;
import icet.koco.global.exception.UnauthorizedException;
import icet.koco.problemSet.dto.ProblemSetResponseDto;
import icet.koco.problemSet.dto.ProblemSolutionResponseDto;
import icet.koco.problemSet.service.ProblemSetService;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "ProblemSet", description = "문제집 관련 API")
@RequestMapping("/api/backend/v1/problem-set")
public class ProblemSetController {

    private final ProblemSetService problemSetService;

    /**
     * 날짜별 문제집 조회
     * @param date
     * @return
     */
    @Operation(summary = "날짜별 문제집 조회")
    @GetMapping
    public ResponseEntity<?> getProblemSetByDate(
        @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        ProblemSetResponseDto response = problemSetService.getProblemSetByDate(userId, date);
        return ResponseEntity.ok(
            ApiResponse.success(ApiResponseCode.SUCCESS,"문제 리스트 조회에 성공하였습니다.", response)
        );
    }

    /**
     * 문제 별 해설 조회
     * @param problemNumber
     * @return
     */
    @Operation(summary = "문제 별 해설 조회")
    @GetMapping("/{problemNumber}/solution")
    public ResponseEntity<?> getProblemSolution(@PathVariable Long problemNumber) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }

        ProblemSolutionResponseDto dto = problemSetService.getProblemSolution(problemNumber);
        return ResponseEntity.ok(
            ApiResponse.success(ApiResponseCode.SUCCESS, "문제 해설 조회에 성공하였습니다.", dto)
        );
    }
}
