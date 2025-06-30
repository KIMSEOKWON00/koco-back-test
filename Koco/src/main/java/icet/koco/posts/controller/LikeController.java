package icet.koco.posts.controller;

import icet.koco.enums.ApiResponseCode;
import icet.koco.global.dto.ApiResponse;
import icet.koco.posts.dto.like.LikeResponseDto;
import icet.koco.posts.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/backend/v3/posts")
@Tag(name = "Likes", description = "좋아요 관련 API")
@RequiredArgsConstructor
public class LikeController {
    private final LikeService likeService;

    @PostMapping("/{postId}/likes")
    @Operation(summary = "좋아요 등록")
    public ResponseEntity<?> createLike(@PathVariable Long postId) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        LikeResponseDto responseDto = likeService.createLike(userId, postId);

        return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.LIKE_SUCCESS, "좋아요가 성공적으로 등록되었습니다.", responseDto));
    }

    @DeleteMapping("/{postId}/likes")
    @Operation(summary = "좋아요 삭제")
    public ResponseEntity<?> deleteLike(@PathVariable Long postId) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        likeService.deleteLike(userId, postId);

        return ResponseEntity.noContent().build();
    }

}
