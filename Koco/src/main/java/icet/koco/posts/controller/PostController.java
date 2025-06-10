package icet.koco.posts.controller;

import static icet.koco.enums.ApiResponseCode.POST_DETAIL_SUCCESS;

import icet.koco.enums.ApiResponseCode;
import icet.koco.global.dto.ApiResponse;
import icet.koco.posts.dto.post.PostCreateEditRequestDto;
import icet.koco.posts.dto.post.PostCreateResponseDto;
import icet.koco.posts.dto.post.PostGetDetailResponseDto;
import icet.koco.posts.dto.post.PostListGetResponseDto;
import icet.koco.posts.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/backend/v3/posts")
@RequiredArgsConstructor
@Tag(name = "Posts", description = "게시판 관련 API들 입니다.")
public class PostController {
    private final PostService postService;

    @PostMapping
    @Operation(summary = "게시글을 등록하는 API입니다.")
    public ResponseEntity<?> createPost(@RequestBody PostCreateEditRequestDto requestDto) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        PostCreateResponseDto responseDto = postService.createPost(userId, requestDto);

        return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.POST_CREATED, "게시글 등록에 성공했습니다.", responseDto));
    }

    @GetMapping("/{postId}")
    @Operation(summary = "게시글 상세 조회를 하는 API 입니다.")
    public ResponseEntity<?> getPostDetail(@PathVariable Long postId) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        PostGetDetailResponseDto responseDto = postService.getPost(userId, postId);

        return ResponseEntity.ok(ApiResponse.success(POST_DETAIL_SUCCESS, "게시글 상세 조회 성공", responseDto));
    }

    @PatchMapping("/{postId}")
    @Operation(summary = "게시글 수정을 하는 API 입니다.")
    public ResponseEntity<?> editPost(@PathVariable Long postId, @RequestBody PostCreateEditRequestDto requestDto) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        postService.editPost(userId, postId, requestDto);

        return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.POST_EDIT_SUCCESS, "게시글 수정 성공", null));
    }

    @DeleteMapping("/{postId}")
    @Operation(summary = "게시글을 삭제하는 API입니다.")
    public ResponseEntity<?> deletePost(@PathVariable Long postId) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        postService.deletePost(userId, postId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "게시글 리스트를 조회하는 API입니다.")
    public ResponseEntity<?> getPostList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<String> category,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(required = false, defaultValue = "10") int size
    ) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (category != null && category.size() > 5) {
            throw new IllegalArgumentException("카테고리는 최대 5개까지 선택할 수 있습니다.");
        }

        PostListGetResponseDto responseDto = postService.getPostList(category, keyword, cursorId, size);

        return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.POST_LIST_SUCCESS, "게시글 리스트 조회에 성공하였습니다.", responseDto));
    }
}

