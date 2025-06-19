package icet.koco.user.controller;

import icet.koco.global.dto.ApiResponse;
import icet.koco.global.exception.UnauthorizedException;
import icet.koco.user.dto.UserAlgorithmStatsResponseDto;
import icet.koco.user.dto.UserInfoRequestDto;
import icet.koco.user.dto.UserInfoResponseDto;
import icet.koco.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@Tag(name = "User", description = "사용자 관련 API")
@RequestMapping("/api/backend/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 유저 탈퇴하기
    @Operation(summary = "사용자 탈퇴")
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<?>> deleteUser(HttpServletResponse response) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        userService.deleteUser(userId, response);

        return ResponseEntity.noContent().build();
    }


    /**
     * 유저 정보 등록 API (초기 등록)
     * @param userInfoRequestDto
     * @return
     */
    @Operation(summary = "유저 정보 등록")
    @PostMapping(value = "/me")
    public ResponseEntity<?> postUserInfo(@RequestBody UserInfoRequestDto userInfoRequestDto) {
        try {
            Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            // DTO 통해서 유저 정보 받아오기
            String nickname = userInfoRequestDto.getNickname();
            String statusMsg = userInfoRequestDto.getStatusMsg();
            String profileImgUrl = userInfoRequestDto.getProfileImgUrl();

            // 유저 정보 설정
            userService.postUserInfo(userId, nickname, statusMsg, profileImgUrl);

            // 204 No content 응답
            return ResponseEntity.noContent().build();

        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("UNAUTHORIZED", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("INTERNAL_SERVER_ERROR", "알 수 없는 서버 에러"));
        }
    }

    /**
     * 유저 정보 수정 API
     * @param userInfoRequestDto
     * @return
     */
    @PatchMapping("/me")
    public ResponseEntity<?> updateUserInfo(@RequestBody UserInfoRequestDto userInfoRequestDto) {
        try {
            Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            String nickname = userInfoRequestDto.getNickname();
            String statusMsg = userInfoRequestDto.getStatusMsg();
            String profileImgUrl = userInfoRequestDto.getProfileImgUrl();

            // 유저 정보 설정
            userService.updateUserInfo(userId, nickname, statusMsg, profileImgUrl);

            // 204 No content 응답
            return ResponseEntity.noContent().build();

        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("UNAUTHORIZED", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("INTERNAL_SERVER_ERROR", "알 수 없는 서버 에러"));
        }
    }


    // 유저 정보 조회
    @Operation(summary = "사용자 정보 조회")
    @GetMapping(value = "/me")
    public ResponseEntity<?> getUserInfo() {
        try {
            Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UserInfoResponseDto userInfoResponseDto = userService.getUserInfo(userId);
            return ResponseEntity.ok(ApiResponse.success("USER_INFO_GET_SUCCESS", "유저 프로필 정보 조회 성공", userInfoResponseDto));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(ApiResponse.fail("SERVER_ERROR", "서버 에러"));
        }
    }


    @Operation(summary = "사용자별 알고리즘 통계 조회")
    @GetMapping("/algorithm-stats")
    public ResponseEntity<?> getAlgorithmStats() {
        try {
            Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UserAlgorithmStatsResponseDto response = userService.getAlgorithmStats(userId);
            return ResponseEntity.ok(ApiResponse.success("USER_ALGORITHM_STATS_GET_SUCCESS", "유저 알고리즘 스탯 정보 조회 성공", response));
        } catch (Exception e) {
            log.error("사용자 알고리즘 스탯 조회 API 에러 발생", e);
            e.printStackTrace(); // 콘솔에 전체 에러 출력
            return ResponseEntity.internalServerError().body(ApiResponse.fail("INTERNAL_SERVER_ERROR", "서버 내부 에러"));
        }
    }
}
