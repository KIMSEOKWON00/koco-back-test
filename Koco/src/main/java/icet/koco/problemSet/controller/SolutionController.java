package icet.koco.problemSet.controller;


import icet.koco.enums.ApiResponseCode;
import icet.koco.global.dto.ApiResponse;
import icet.koco.problemSet.dto.AiSolutionRequestDto;
import icet.koco.problemSet.service.SolutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Solution", description = "해설 관련 API | 백 - AI")
@RequestMapping("/api/backend/v1/solution")
@RequiredArgsConstructor
public class SolutionController {

    private final SolutionService solutionService;

    /**
     * 해설 저장 API (AI -> 백)
     * @param aiSolutionRequestDto
     * @return
     */
    @Operation(summary = "AI서버로부터 해설 저장")
    @PostMapping
    public ResponseEntity<?> receiveSolution(@RequestBody AiSolutionRequestDto aiSolutionRequestDto) {
        solutionService.saveFromAi(aiSolutionRequestDto);
        return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.SUCCESS, "해설 저장 완료", null));
    }
}