package icet.koco.fixture;

import icet.koco.posts.dto.post.PostCreateEditRequestDto;
import icet.koco.posts.entity.Post;
import icet.koco.posts.entity.PostCategory;
import icet.koco.problemSet.entity.Category;
import icet.koco.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public class PostFixture {

	public static final String TEST_TITLE = "테스트 제목";
	public static final String TEST_CONTENT = "테스트 내용";
	public static final String UPDATED_TITLE = "수정된 제목";
	public static final String UPDATED_CONTENT = "수정된 내용";

	public static Post post(Long postId, User user, Long problemNumber) {
		return Post.builder()
			.id(postId)
			.user(user)
			.problemNumber(problemNumber)
			.title(TEST_TITLE)
			.content(TEST_CONTENT)
			.commentCount(5)
			.likeCount(5)
			.createdAt(LocalDateTime.of(2025, 6, 10, 12, 0))
			.build();
	}

	public static Post postWithCategory(User user, Category category, Long postId, Long problemNumber) {
		Post post = Post.builder()
			.id(postId)
			.user(user)
			.problemNumber(problemNumber)
			.title(TEST_TITLE)
			.content(TEST_CONTENT)
			.commentCount(5)
			.likeCount(5)
			.createdAt(LocalDateTime.of(2025, 6, 10, 12, 0))
			.build();

		PostCategory postCategory = PostCategory.builder()
			.category(category)
			.build();

		post.addPostCategory(postCategory);
		return post;
	}

	public static PostCreateEditRequestDto requestDto(Long problemNumber, List<String> categories, String UPDATED_TITLE, String UPDATED_CONTENT) {
		return PostCreateEditRequestDto.builder()
			.problemNumber(problemNumber)
			.title(UPDATED_TITLE)
			.content(UPDATED_CONTENT)
			.category(categories)
			.build();
	}


}
