package icet.koco.post;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verify;

import icet.koco.enums.ErrorMessage;
import icet.koco.fixture.CategoryFixture;
import icet.koco.fixture.PostFixture;
import icet.koco.fixture.ProblemFixture;
import icet.koco.fixture.UserFixture;
import icet.koco.global.exception.BadRequestException;
import icet.koco.global.exception.ForbiddenException;
import icet.koco.global.exception.ResourceNotFoundException;
import icet.koco.posts.dto.post.PostCreateEditRequestDto;
import icet.koco.posts.dto.post.PostCreateResponseDto;
import icet.koco.posts.dto.post.PostGetDetailResponseDto;
import icet.koco.posts.entity.Post;
import icet.koco.problemSet.entity.Category;
import icet.koco.problemSet.entity.Problem;
import java.util.List;
import java.util.Optional;
import icet.koco.posts.repository.CommentRepository;
import icet.koco.posts.repository.LikeRepository;
import icet.koco.posts.repository.PostCategoryRepository;
import icet.koco.posts.repository.PostRepository;
import icet.koco.posts.service.PostService;
import icet.koco.problemSet.repository.CategoryRepository;
import icet.koco.problemSet.repository.ProblemCategoryRepository;
import icet.koco.problemSet.repository.ProblemRepository;
import icet.koco.user.entity.User;
import icet.koco.user.repository.UserRepository;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class PostServiceTest {

	private static final Logger log = LoggerFactory.getLogger(PostServiceTest.class);
	@InjectMocks
	private PostService postService;

	@Mock
	private PostRepository postRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private CategoryRepository categoryRepository;

	@Mock
	private CommentRepository commentRepository;

	@Mock
	private LikeRepository likeRepository;

	@Mock
	private ProblemRepository problemRepository;

	@Mock
	private ProblemCategoryRepository problemCategoryRepository;

	@Mock
	private PostCategoryRepository postCategoryRepository;

	private static final Long VALID_USER_ID = 1L;
	private static final Long INVALID_USER_ID = 999L;
	private static final Long VALID_POST_ID = 100L;
	private static final Long INVALID_POST_ID = 999L;
	private static final Long VALID_PROBLEM_NUMBER = 30000L;
	private static final Long INVALID_PROBLEM_NUMBER = 9999L;
	private static final String CATEGORY_DP = "dp";
	private static final List<String> VALID_CATEGORIES = List.of(CATEGORY_DP);
	private static final List<String> INVALID_CATEGORIES = List.of("test", CATEGORY_DP);

	private User user;
	private Post post;
	private Category category;
	private Problem problem;
	private PostCreateEditRequestDto requestDto;

	@BeforeEach
	void setUp() {
		user = UserFixture.validUser();
		category = CategoryFixture.category(1L, CATEGORY_DP);
		problem = ProblemFixture.problem(1L, VALID_PROBLEM_NUMBER);
		post = PostFixture.postWithCategory(user, category, VALID_POST_ID, VALID_PROBLEM_NUMBER);
	}

	@Nested
	@DisplayName("게시글 생성")
	class createPostTest {

		@Test
		void createPost_성공() {
			// given
			given(userRepository.findById(VALID_USER_ID)).willReturn(Optional.of(user));
			given(problemRepository.findByNumber(VALID_PROBLEM_NUMBER)).willReturn(
				Optional.of(problem));
			given(categoryRepository.findByNameIn(VALID_CATEGORIES)).willReturn(List.of(category));
			PostCreateEditRequestDto requestDto = PostFixture.requestDto(VALID_PROBLEM_NUMBER,
				VALID_CATEGORIES, PostFixture.TEST_TITLE, PostFixture.TEST_CONTENT);

			// when
			PostCreateResponseDto responseDto = postService.createPost(VALID_USER_ID, requestDto);

			// then
			assertThat(responseDto).isNotNull();

			ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
			then(postRepository).should().save(postCaptor.capture());

			Post savedPost = postCaptor.getValue();
			assertThat(savedPost.getTitle()).isEqualTo(PostFixture.TEST_TITLE);
			assertThat(savedPost.getContent()).isEqualTo(PostFixture.TEST_CONTENT);
			assertThat(savedPost.getUser()).isEqualTo(user);
			assertThat(savedPost.getProblemNumber()).isEqualTo(VALID_PROBLEM_NUMBER);
			assertThat(savedPost.getPostCategories().get(0).getCategory().getName()).isEqualTo(
				"dp");
		}

		@ParameterizedTest(name = "[{index}] {5}")
		@MethodSource("invalidCreatePostArgs")
		void createPost_예외(
			Long userId,
			Long problemNumber,
			List<String> categories,
			Class<? extends RuntimeException> expectedException,
			ErrorMessage errorMessage,
			String caseDescription) {

			PostCreateEditRequestDto requestDto = PostFixture.requestDto(problemNumber, categories, PostFixture.TEST_TITLE, PostFixture.TEST_CONTENT);

			// 존재하지 않는 사용자 ID라면 empty
			given(userRepository.findById(userId)).willReturn(
				userId.equals(VALID_USER_ID) ? Optional.of(user) : Optional.empty()
			);

			// 존재하지 않는 문제번호라면 empty
			given(problemRepository.findByNumber(problemNumber)).willReturn(
				problemNumber.equals(VALID_PROBLEM_NUMBER) ? Optional.of(problem) : Optional.empty()
			);

			given(categoryRepository.findByNameIn(categories)).willReturn(List.of(category));

			assertThatThrownBy(() -> postService.createPost(userId, requestDto))
				.isInstanceOf(expectedException)
				.hasMessage(errorMessage.getMessage());
		}

		static Stream<Arguments> invalidCreatePostArgs() {
			return Stream.of(
				// 존재하지 않는 사용자
				Arguments.of(INVALID_USER_ID, VALID_PROBLEM_NUMBER, VALID_CATEGORIES, ForbiddenException.class, ErrorMessage.USER_NOT_FOUND, "존재하지 않는 사용자"),

				// 존재하지 않는 문제번호
				Arguments.of(VALID_USER_ID, INVALID_PROBLEM_NUMBER, VALID_CATEGORIES, BadRequestException.class, ErrorMessage.INVALID_PROBLEM_INCLUDED, "존재하지 않는 문제번호"),

				// 존재하지 않는 카테고리
				Arguments.of(VALID_USER_ID, VALID_PROBLEM_NUMBER, INVALID_CATEGORIES, BadRequestException.class, ErrorMessage.INVALID_CATEGORY_INCLUDED, "존재하지 않는 카테고리")
			);
		}

		@Nested
		@DisplayName("게시글 조회")
		class getPostTest {

			@Test
			void getPost_성공() {
				// given
				given(postRepository.findByIdWithUserAndCategories(VALID_POST_ID)).willReturn(
					Optional.of(post));
				given(likeRepository.countByPostId(VALID_POST_ID)).willReturn(5);
				given(commentRepository.countByPostIdAndDeletedAtIsNull(VALID_POST_ID)).willReturn(5);
				given(likeRepository.existsByUserIdAndPostId(VALID_USER_ID, VALID_POST_ID)).willReturn(false);

				// when
				PostGetDetailResponseDto responseDto = postService.getPost(VALID_USER_ID,
					VALID_POST_ID);

				// then
				assertThat(responseDto).isNotNull();
				assertThat(responseDto.getPostId()).isEqualTo(VALID_POST_ID);
				assertThat(responseDto.getTitle()).isEqualTo(PostFixture.TEST_TITLE);
				assertThat(responseDto.getContent()).isEqualTo(PostFixture.TEST_CONTENT);
				assertThat(responseDto.getLikeCount()).isEqualTo(5);
				assertThat(responseDto.getCommentCount()).isEqualTo(5);
				assertThat(responseDto.isLiked()).isFalse();
				assertThat(responseDto.getCategories().size()).isEqualTo(1);
				assertThat(responseDto.getCategories().get(0).getCategoryName()).isEqualTo(
					CATEGORY_DP);
				assertThat(responseDto.getAuthor().getNickname()).isEqualTo(user.getNickname());
				assertThat(responseDto.getAuthor().getUserId()).isEqualTo(VALID_USER_ID);
			}

			@Test
			void getPost_존재하지않는게시글() {
				// given
				given(postRepository.findByIdWithUserAndCategories(VALID_POST_ID)).willReturn(
					Optional.empty());

				// then & when
				assertThatThrownBy(() -> postService.getPost(VALID_USER_ID, VALID_POST_ID))
					.isInstanceOf(ResourceNotFoundException.class)
					.hasMessage(ErrorMessage.POST_NOT_FOUND.getMessage());
			}
		}

		@Nested
		@DisplayName("게시글 수정")
		class editPostTest {

			@Test
			@DisplayName("게시글 수정 성공")
			void editPost_성공() {
				PostCreateEditRequestDto requestDto = PostFixture.requestDto(VALID_PROBLEM_NUMBER,
					VALID_CATEGORIES, PostFixture.UPDATED_TITLE, PostFixture.UPDATED_CONTENT);

				given(postRepository.findByIdWithUserAndCategories(VALID_POST_ID)).willReturn(
					Optional.of(post));
				given(problemRepository.findByNumber(VALID_PROBLEM_NUMBER)).willReturn(
					Optional.of(problem));
				given(categoryRepository.findByNameIn(VALID_CATEGORIES)).willReturn(
					List.of(category));

				postService.editPost(VALID_USER_ID, VALID_POST_ID, requestDto);

				assertThat(post.getProblemNumber()).isEqualTo(VALID_PROBLEM_NUMBER);
				assertThat(post.getTitle()).isEqualTo(PostFixture.UPDATED_TITLE);
				assertThat(post.getContent()).isEqualTo(PostFixture.UPDATED_CONTENT);
				assertThat(post.getPostCategories().get(0).getCategory().getName()).isEqualTo(
					CATEGORY_DP);
				assertThat(post.getUpdatedAt()).isNotNull();
			}

			@ParameterizedTest(name = "[{index}] {6}")
			@MethodSource("invalidEditPostArgs")
			void editPost_예외(
				Long userId, Long postId, Long problemNumber, List<String> categories,
				Class<? extends RuntimeException> expectedException, ErrorMessage errorMessage,
				String caseDescription
			) {
				PostCreateEditRequestDto requestDto = PostFixture.requestDto(problemNumber,
					categories,
					PostFixture.UPDATED_TITLE, PostFixture.UPDATED_CONTENT);

				if (!postId.equals(INVALID_POST_ID)) {
					post.setUser(user);
					if (!userId.equals(user.getId())) {
						post.setUser(UserFixture.anotherUser()); // 다른 유저로 변경
					}
					given(postRepository.findByIdWithUserAndCategories(postId)).willReturn(
						Optional.of(post));
				} else {
					given(postRepository.findByIdWithUserAndCategories(postId)).willReturn(
						Optional.empty());
				}

				// 문제 존재 여부
				if (!problemNumber.equals(INVALID_PROBLEM_NUMBER)) {
					given(problemRepository.findByNumber(problemNumber)).willReturn(
						Optional.of(problem));
				} else {
					given(problemRepository.findByNumber(problemNumber)).willReturn(
						Optional.empty());
				}

				// 카테고리 존재 여부
				if (!categories.equals(INVALID_CATEGORIES)) {
					given(categoryRepository.findByNameIn(categories)).willReturn(
						List.of(category));
				} else {
					given(categoryRepository.findByNameIn(categories)).willReturn(
						List.of(category));
				}

				// when & then
				assertThatThrownBy(() -> postService.editPost(userId, postId, requestDto))
					.isInstanceOf(expectedException)
					.hasMessage(errorMessage.getMessage());
			}

			static Stream<Arguments> invalidEditPostArgs() {
				return Stream.of(
					// 수정 권한 없음
					Arguments.of(INVALID_USER_ID, VALID_POST_ID, VALID_PROBLEM_NUMBER,
						VALID_CATEGORIES, ForbiddenException.class, ErrorMessage.NO_POST_PERMISSION,
						"수정권한 없음"),

					// 게시글 없음
					Arguments.of(VALID_USER_ID, INVALID_POST_ID, VALID_PROBLEM_NUMBER,
						VALID_CATEGORIES, ResourceNotFoundException.class,
						ErrorMessage.POST_NOT_FOUND, "존재하지 않는 게시글"),

					// 존재하지 않는 문제번호
					Arguments.of(VALID_USER_ID, VALID_POST_ID, INVALID_PROBLEM_NUMBER,
						VALID_CATEGORIES, BadRequestException.class,
						ErrorMessage.INVALID_PROBLEM_INCLUDED, "존재하지 않는 문제 번호"),

					// 존재하지 않는 카테고리
					Arguments.of(VALID_USER_ID, VALID_POST_ID, VALID_PROBLEM_NUMBER,
						INVALID_CATEGORIES, BadRequestException.class,
						ErrorMessage.INVALID_CATEGORY_INCLUDED, "존재하지 않는 카테고리")
				);
			}
		}

		@Nested
		@DisplayName("게시글 삭제")
		class deletePostTest {

			@Test
			void deletePost_성공() {
				// given
				given(postRepository.findByIdWithUser(VALID_POST_ID)).willReturn(Optional.of(post));

				// when
				postService.deletePost(VALID_USER_ID, VALID_POST_ID);

				// then
				assertThat(post.getDeletedAt()).isNotNull();
				verify(postRepository).findByIdWithUser(VALID_POST_ID);
			}

			@ParameterizedTest(name = "[{index}] {3}")
			@MethodSource("deletePostExceptionArgs")
			void deletePost_예외(
				Long userId,
				Post postToFind,
				Class<? extends RuntimeException> expectedException,
				String caseDescription
			) {
				// given
				if (postToFind != null) {
					given(postRepository.findByIdWithUser(VALID_POST_ID)).willReturn(Optional.of(postToFind));
				} else {
					given(postRepository.findByIdWithUser(VALID_POST_ID)).willReturn(Optional.empty());
				}

				// when & then
				assertThatThrownBy(() -> postService.deletePost(userId, VALID_POST_ID))
					.isInstanceOf(expectedException)
					.hasMessage(expectedException == ForbiddenException.class
						? ErrorMessage.NO_POST_PERMISSION.getMessage()
						: ErrorMessage.POST_NOT_FOUND.getMessage());
			}

			static Stream<Arguments> deletePostExceptionArgs() {
				Post postOwnedByAnotherUser = PostFixture.post(VALID_POST_ID, UserFixture.anotherUser(), VALID_PROBLEM_NUMBER);

				return Stream.of(
					Arguments.of(VALID_USER_ID, null, ResourceNotFoundException.class, "존재하지 않는 게시글"),
					Arguments.of(VALID_USER_ID, postOwnedByAnotherUser, ForbiddenException.class, "삭제 권한 없음")
				);
			}
		}
	}
}
