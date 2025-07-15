package icet.koco.posts.service;

import static java.time.LocalDateTime.now;

import icet.koco.enums.ErrorMessage;
import icet.koco.global.exception.BadRequestException;
import icet.koco.global.exception.ForbiddenException;
import icet.koco.global.exception.ResourceNotFoundException;
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
			.orElseThrow(() -> new ForbiddenException(ErrorMessage.USER_NOT_FOUND));

		List<Category> categories = categoryRepository.findByNameIn(requestDto.getCategory());

		// 해당 문제가 존재하는지 확인
		problemRepository.findByNumber(requestDto.getProblemNumber())
			.orElseThrow(() -> new BadRequestException(
				ErrorMessage.INVALID_PROBLEM_INCLUDED
			));

		// 카테고리가 존재하는지 확인
		if (categories.size() != requestDto.getCategory().size()) {
			throw new BadRequestException(ErrorMessage.INVALID_CATEGORY_INCLUDED);
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
			.orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.POST_NOT_FOUND));

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
			.orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.POST_NOT_FOUND));

		// 권한 체크
		if (!post.getUser().getId().equals(userId)) {
			throw new ForbiddenException(ErrorMessage.NO_POST_PERMISSION);
		}

		// 내용 저장
		if (requestDto.getProblemNumber() != null) {
			// 해당 문제가 존재하는지 확인
			problemRepository.findByNumber(requestDto.getProblemNumber())
				.orElseThrow(() -> new BadRequestException(ErrorMessage.INVALID_PROBLEM_INCLUDED));
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
				throw new BadRequestException(ErrorMessage.INVALID_CATEGORY_INCLUDED);
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
			.orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.POST_NOT_FOUND));

		// 권한 체크
		if (!post.getUser().getId().equals(userId)) {
			throw new ForbiddenException(ErrorMessage.NO_POST_PERMISSION);
		}

		// softDelete 처리
		post.setDeletedAt(now());
	}

	@Transactional(readOnly = true)
	public PostListGetResponseDto getPostList(List<String> category, String keyword, Long cursorId, int size) {
		List<Post> posts;

		posts = postRepository.searchPosts(category, keyword, cursorId, size + 1);

		boolean hasNext = posts.size() > size;

		Long nextCursorId = null;
		if (hasNext) {
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
