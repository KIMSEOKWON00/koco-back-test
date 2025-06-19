package icet.koco.admin.controller;

import icet.koco.admin.dto.TodayProblemSetRequestDto;
import icet.koco.admin.service.AdminService;
import icet.koco.enums.ApiResponseCode;
import icet.koco.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/backend/admin")
@RequiredArgsConstructor
@Tag(name = "admin", description = "관리자용 API들입니다.")
public class AdminController {
    private final AdminService adminService;

    @PostMapping("/today/problem-set")
    @Operation(summary = "오늘의 문제집을 생성하는 API입니다.")
    public ResponseEntity<?> createTodayProblemset(@RequestBody TodayProblemSetRequestDto requestDto) {
        Long problemsetId = adminService.createTodayProblemset(requestDto);

        return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.SUCCESS, "오늘의 문제집 매핑 완료", problemsetId));
    }
}
