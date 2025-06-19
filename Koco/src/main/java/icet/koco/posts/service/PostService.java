package icet.koco.posts.service;

import static java.time.LocalDateTime.now;

import icet.koco.global.exception.ForbiddenException;
import icet.koco.global.exception.ResourceNotFoundException;
import icet.koco.global.exception.UnauthorizedException;
import icet.koco.posts.dto.post.PostCreateEditRequestDto;
import icet.koco.posts.dto.post.PostCreateResponseDto;
import icet.koco.posts.dto.post.PostGetDetailResponseDto;
import icet.koco.posts.dto.post.PostListGetResponseDto;
import icet.koco.posts.entity.Post;
import icet.koco.posts.entity.PostCategory;
import icet.koco.posts.repository.CommentRepository;
import icet.koco.posts.repository.LikeRepository;
import icet.koco.posts.repository.PostCategoryRepository;
import icet.koco.posts.repository.PostRepository;
import icet.koco.problemSet.entity.Category;
import icet.koco.problemSet.repository.CategoryRepository;
import icet.koco.problemSet.repository.ProblemRepository;
import icet.koco.user.entity.User;
import icet.koco.user.repository.UserRepository;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final PostCategoryRepository postCategoryRepository;
    private final ProblemRepository problemRepository;

    /**
     * 게시글 등록
     *
     * @param userId     유저Id
     * @param requestDto (number, title, content, category)
     * @return postId
     */
    @Transactional
    public PostCreateResponseDto createPost(Long userId, PostCreateEditRequestDto requestDto) {
        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ForbiddenException("존재하지 않는 사용자입니다."));

        List<Category> categories = categoryRepository.findByNameIn(requestDto.getCategory());

        // 해당 문제가 존재하는지 확인
        problemRepository.findByNumber(requestDto.getProblemNumber())
                .orElseThrow(() -> new IllegalArgumentException(
                        "해당 문제 번호를 가진 백준 문제가 없습니다."));

        if (categories.size() != requestDto.getCategory().size()) {
            throw new IllegalArgumentException("존재하지 않는 카테고리가 포함되어 있습니다.");
        }

        // Post Entity 생성
        Post post = Post.builder()
                .user(user)
                .problemNumber(requestDto.getProblemNumber())
                .commentCount(0)
                .likeCount(0)
                .content(requestDto.getContent())
                .title(requestDto.getTitle())
                .createdAt(now())
                .build();

        postRepository.save(post);

        // 중간테이블에 카테고리 매핑
        for (Category category : categories) {
            PostCategory postCategory = PostCategory.builder()
                    .post(post)
                    .category(category)
                    .build();
            post.addPostCategory(postCategory);
            postCategoryRepository.save(postCategory);
        }

        // DTO에 담아 반환
        return PostCreateResponseDto.builder()
                .postId(post.getId())
                .build();
    }

    /**
     * 게시글 상세 조회
     *
     * @param postId 게시글 Id
     * @return PostGetDetailResponseDto
     */
    @Transactional(readOnly = true)
    public PostGetDetailResponseDto getPost(Long userId, Long postId) {
        // 게시글 찾기
        Post post = postRepository.findByIdWithUserAndCategories(postId)
                .orElseThrow(() -> new ForbiddenException("해당 게시글이 존재하지 않습니다."));

        // 댓글, 좋아요 수
        Integer likeCount = likeRepository.countByPostId(postId);
        Integer commentCount = commentRepository.countByPostIdAndDeletedAtIsNull(postId);

        // 로그인된 유저가 좋아요를 눌렀는지 여부
        boolean liked = likeRepository.existsByUserIdAndPostId(userId, postId);

        // DTO에 맞춰서 반환
        return PostGetDetailResponseDto.builder()
                .postId(postId)
                .title(post.getTitle())
                .problemNumber(post.getProblemNumber())
                .createdAt(post.getCreatedAt())
                .categories(
                        post.getPostCategories().stream()
                                .map(c -> new PostGetDetailResponseDto.CategoryDto(c.getCategory().getId(), c.getCategory().getName()))
                                .toList()
                )
                .content(post.getContent())
                .author(PostGetDetailResponseDto.AuthorDto.builder()
                        .userId(post.getUser().getId())
                        .nickname(post.getUser().getNickname())
                        .imgUrl(post.getUser().getProfileImgUrl())
                        .build())
                .commentCount(commentCount)
                .likeCount(likeCount)
                .liked(liked)
                .build();
    }

    /**
     * 게시글 수정
     *
     * @param userId     로그인된 유저의 Id
     * @param postId     게시글 Id
     * @param requestDto (problemNumber, title, content, categories)
     */
    @Transactional
    public void editPost(Long userId, Long postId, PostCreateEditRequestDto requestDto) {
        Post post = postRepository.findByIdWithUserAndCategories(postId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 게시글이 존재하지 않습니다."));

        // 권한 체크
        if (!post.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("게시글 수정 권한이 없습니다.");
        }

        // 내용 저장
        if (requestDto.getProblemNumber() != null) {
            // 해당 문제가 존재하는지 확인
            problemRepository.findByNumber(requestDto.getProblemNumber())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "해당 문제 번호를 가진 Problem이 없습니다: " + requestDto.getProblemNumber()));
            post.setProblemNumber(requestDto.getProblemNumber());

        }

        if (requestDto.getTitle() != null) {
            post.setTitle(requestDto.getTitle());
        }

        if (requestDto.getContent() != null) {
            post.setContent(requestDto.getContent());
        }

        if (requestDto.getCategory() != null) {
            post.getPostCategories().clear();
            postCategoryRepository.deleteByPost(post);

            // 카테고리 이름으로 Category 조회
            List<Category> categories = categoryRepository.findByNameIn(requestDto.getCategory());

            if (categories.size() != requestDto.getCategory().size()) {
                throw new IllegalArgumentException("존재하지 않는 카테고리가 포함되어 있습니다.");
            }

            // 중간테이블에 카테고리 매핑
            for (Category category : categories) {
                PostCategory postCategory = PostCategory.builder()
                        .category(category)
                        .build();
                post.addPostCategory(postCategory);
            }
        }

        // 수정된 시간 저장
        post.setUpdatedAt(now());
    }

    /**
     * 게시글 삭제
     *
     * @param userId 로그인된 유저의 Id
     * @param postId 게시글 Id
     */
    @Transactional
    public void deletePost(Long userId, Long postId) {
        Post post = postRepository.findByIdWithUser(postId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 게시글이 존재하지 않습니다."));

        // 권한 체크
        if (!post.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("게시글 삭제 권한이 없습니다.");
        }

        // softDelete 처리
        post.setDeletedAt(now());
        log.info("softDelete post: {}", post);
    }

    @Transactional(readOnly = true)
    public PostListGetResponseDto getPostList(List<String> category, String keyword, Long cursorId, int size) {
        List<Post> posts;

        posts = postRepository.searchPosts(category, keyword, cursorId, size + 1);

//        posts = postRepository.findAllByDeletedAtIsNullOrderByCreatedAtDesc();
        boolean hasNext = posts.size() > size;

        Long nextCursorId = null;
        if (hasNext) {
//            nextCursorId = posts.get(size).getId();             // 잘리기 전에 다음 커서 미리 저장
//            posts = posts.subList(0, size);                     // 그리고 자르기
            Post lastPost = posts.remove(size); // 이건 remove(int index) → Post 리턴
            nextCursorId = lastPost.getId();
        }

        List<PostListGetResponseDto.PostDetailDto> postDtos = posts.stream()
                .map(post -> PostListGetResponseDto.PostDetailDto.builder()
                        .postId(post.getId())
                        .problemNumber(post.getProblemNumber())
                        .title(post.getTitle())
                        .createdAt(post.getCreatedAt())
                        .categories(post.getPostCategories().stream()
                                .map(pc -> PostListGetResponseDto.CategoryDto.builder()
                                        .categoryId(pc.getCategory().getId())
                                        .categoryName(pc.getCategory().getName())
                                        .build())
                                .toList())
                        .author(PostListGetResponseDto.AuthorDto.builder()
                                .userId(post.getUser().getId())
                                .nickname(post.getUser().getNickname())
                                .imgUrl(post.getUser().getProfileImgUrl())
                                .build())
                        .commentCount(post.getCommentCount())
                        .likeCount(post.getLikeCount())
                        .build())
                .toList();

        return PostListGetResponseDto.builder()
                .nextCursorId(nextCursorId)
                .hasNext(hasNext)
                .posts(postDtos)
                .build();
    }

    public PostListGetResponseDto getMyPostList(Long userId, Long cursorId, int size) {
        List<Post> posts = postRepository.getMyPosts(userId, cursorId, size + 1);

        boolean hasNext = posts.size() > size;
        Long nextCursorId = null;

        if (hasNext) {
            Post lastPost = posts.remove(size);
            nextCursorId = lastPost.getId();
        }

        List<PostListGetResponseDto.PostDetailDto> postDtos = posts.stream()
                .map(post -> PostListGetResponseDto.PostDetailDto.builder()
                        .postId(post.getId())
                        .problemNumber(post.getProblemNumber())
                        .title(post.getTitle())
                        .createdAt(post.getCreatedAt())
                        .categories(post.getPostCategories().stream()
                                .map(pc -> PostListGetResponseDto.CategoryDto.builder()
                                        .categoryId(pc.getCategory().getId())
                                        .categoryName(pc.getCategory().getName())
                                        .build())
                                .toList())
                        .author(PostListGetResponseDto.AuthorDto.builder()
                                .userId(post.getUser().getId())
                                .nickname(post.getUser().getNickname())
                                .imgUrl(post.getUser().getProfileImgUrl())
                                .build())
                        .commentCount(post.getCommentCount())
                        .likeCount(post.getLikeCount())
                        .build())
                .toList();

        return PostListGetResponseDto.builder()
                .nextCursorId(nextCursorId)
                .hasNext(hasNext)
                .posts(postDtos)
                .build();
    }
}
