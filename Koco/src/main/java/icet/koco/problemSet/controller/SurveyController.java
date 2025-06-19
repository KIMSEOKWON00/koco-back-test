package icet.koco.problemSet.controller;

import icet.koco.global.dto.ApiResponse;
import icet.koco.problemSet.dto.ProblemSetSurveyRequestDto;
import icet.koco.problemSet.dto.SurveyResponseDto;
import icet.koco.problemSet.service.SurveyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Survey", description = "사용자 설문 관련 API")
@RequestMapping("/api/backend/v1/problem-set/surveys")
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyService surveyService;

    @Operation(summary = "설문 응답 저장")
    @PostMapping
    public ResponseEntity<ApiResponse<SurveyResponseDto>> submitSurvey(
        @RequestBody ProblemSetSurveyRequestDto requestDto) {

        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        SurveyResponseDto response = surveyService.submitSurvey(userId, requestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.success("SURVEY_CREATED", "출제 문제집에 대한 설문응답이 성공적으로 생성되었습니다.", response)
        );
    }
}
