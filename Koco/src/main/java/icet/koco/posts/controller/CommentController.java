package icet.koco.posts.controller;


import icet.koco.enums.ApiResponseCode;
import icet.koco.global.dto.ApiResponse;
import icet.koco.posts.dto.comment.CommentCreateEditRequestDto;
import icet.koco.posts.dto.comment.CommentCreateEditResponseDto;
import icet.koco.posts.dto.comment.CommentListResponseDto;
import icet.koco.posts.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/api/backend/v3/posts")
@RestController
@RequiredArgsConstructor
@Tag(name = "Comment", description = "댓글 관련 API 입니다.")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/{postId}/comments")
    @Operation(summary = "댓글 등록하는 API입니다.")
    public ResponseEntity<?> createComment(@PathVariable Long postId, @RequestBody CommentCreateEditRequestDto requestDto) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        CommentCreateEditResponseDto responseDto = commentService.createComment(userId, postId, requestDto);

        return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.COMMENT_CREATED, "댓글이 성공적으로 등록되었습니다.", responseDto));
    }

    @PutMapping("/{postId}/comments/{commentId}")
    @Operation(summary = "댓글 수정하는 API입니다.")
    public ResponseEntity<?> editComment(@PathVariable Long postId, @PathVariable Long commentId, @RequestBody CommentCreateEditRequestDto requestDto) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        CommentCreateEditResponseDto responseDto = commentService.editComment(userId, postId, commentId, requestDto);

        return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.COMMENT_EDIT_SUCCESS, "댓글이 성공적으로 수정되었습니다.", responseDto));

    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    @Operation(summary = "댓글 삭제하는 API입니다.")
    public ResponseEntity<?> deleteComment(@PathVariable Long postId, @PathVariable Long commentId) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        commentService.deleteComment(userId, postId, commentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{postId}/comments")
    @Operation(summary = "게시글의 댓글 리스트 조회 (커서 기반)")
    public ResponseEntity<?> getComments(
        @PathVariable Long postId,
        @RequestParam(required = false) Long cursorId,
        @RequestParam(defaultValue = "10") Integer size) {

        CommentListResponseDto response = commentService.getComments(postId, cursorId, size);

        return ResponseEntity.ok(ApiResponse.success(
            ApiResponseCode.COMMENT_LIST_SUCCESS,
            "댓글 리스트 조회 성공",
            response));
    }
}
