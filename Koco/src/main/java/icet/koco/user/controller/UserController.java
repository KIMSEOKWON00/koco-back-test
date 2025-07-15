package icet.koco.user.controller;

import icet.koco.enums.ApiResponseCode;
import icet.koco.global.dto.ApiResponse;
import icet.koco.posts.dto.post.PostListGetResponseDto;
import icet.koco.posts.service.PostService;
import icet.koco.user.dto.UserAlgorithmStatsResponseDto;
import icet.koco.user.dto.UserInfoRequestDto;
import icet.koco.user.dto.UserInfoResponseDto;
import icet.koco.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final PostService postService;

    /**
     * 사용자 탈퇴 API
     *
     * @param response
     * @return
     */
    @Operation(summary = "사용자 탈퇴")
    @DeleteMapping("/me")
    public ResponseEntity<?> deleteUser(HttpServletResponse response) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        userService.deleteUser(userId, response);

        return ResponseEntity.noContent().build();
    }


    /**
     * 유저 정보 등록 API (초기 등록)
     *
     * @param userInfoRequestDto
     * @return
     */
    @Operation(summary = "유저 정보 등록")
    @PostMapping(value = "/me")
    public ResponseEntity<?> postUserInfo(@RequestBody UserInfoRequestDto userInfoRequestDto) {

        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // DTO 통해서 유저 정보 받아오기
        String nickname = userInfoRequestDto.getNickname();
        String statusMsg = userInfoRequestDto.getStatusMsg();
        String profileImgUrl = userInfoRequestDto.getProfileImgUrl();

        // 유저 정보 설정
        userService.postUserInfo(userId, nickname, statusMsg, profileImgUrl);

        // 204 No content 응답
        return ResponseEntity.noContent().build();
    }

    /**
     * 유저 정보 수정 API
     *
     * @param userInfoRequestDto
     * @return
     */
    @PatchMapping("/me")
    public ResponseEntity<?> updateUserInfo(@RequestBody UserInfoRequestDto userInfoRequestDto) {

        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String nickname = userInfoRequestDto.getNickname();
        String statusMsg = userInfoRequestDto.getStatusMsg();
        String profileImgUrl = userInfoRequestDto.getProfileImgUrl();

        // 유저 정보 설정
        userService.updateUserInfo(userId, nickname, statusMsg, profileImgUrl);

        // 204 No content 응답
        return ResponseEntity.noContent().build();
    }


    /**
     * 사용자 정보 조회 API
     *
     * @return
     */
    @Operation(summary = "사용자 정보 조회")
    @GetMapping(value = "/me")
    public ResponseEntity<?> getUserInfo() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        UserInfoResponseDto userInfoResponseDto = userService.getUserInfo(userId);
        return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.SUCCESS, "유저 프로필 정보 조회 성공", userInfoResponseDto));
    }


    @Operation(summary = "사용자별 알고리즘 통계 조회")
    @GetMapping("/algorithm-stats")
    public ResponseEntity<?> getAlgorithmStats() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        UserAlgorithmStatsResponseDto response = userService.getAlgorithmStats(userId);
        return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.SUCCESS, "유저 알고리즘 스탯 정보 조회 성공", response));
    }

    @Operation(summary = "유저가 작성한 게시글 리스트 조회")
    @GetMapping("/myposts")
    public ResponseEntity<?> getMyPosts(
            @RequestParam(required = false) Long cursorId,
            @RequestParam(required = false, defaultValue = "10") int size) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        PostListGetResponseDto responseDto = postService.getMyPostList(userId, cursorId, size);

        return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.MY_POST_LIST_SUCCESS, "내가 작성한 게시물 조회 성공", responseDto));
    }
}