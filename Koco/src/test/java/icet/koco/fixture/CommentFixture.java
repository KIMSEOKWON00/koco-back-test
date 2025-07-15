package icet.koco.fixture;

import icet.koco.posts.dto.comment.CommentCreateEditRequestDto;
import icet.koco.posts.entity.Comment;
import icet.koco.posts.entity.Post;
import icet.koco.user.entity.User;
import java.time.LocalDateTime;

public class CommentFixture {

	/**
	 * 기본 댓글 생성 (ID 없이)
	 */
	public static Comment comment(User user, Post post, String content) {
		return Comment.builder()
			.user(user)
			.post(post)
			.comment(content)
			.createdAt(LocalDateTime.now())
			.build();
	}

	/**
	 * ID를 가진 댓글 생성
	 */
	public static Comment comment(Long id, User user, Post post, String content) {
		return Comment.builder()
			.id(id)
			.user(user)
			.post(post)
			.comment(content)
			.createdAt(LocalDateTime.now())
			.build();
	}

	/**
	 * 삭제된 댓글
	 */
	public static Comment deletedComment(Long id, User user, Post post, String content) {
		return Comment.builder()
			.id(id)
			.user(user)
			.post(post)
			.comment(content)
			.createdAt(LocalDateTime.now())
			.deletedAt(LocalDateTime.now())
			.build();
	}

	public static CommentCreateEditRequestDto requestDto(String content) {
		return CommentCreateEditRequestDto.builder()
			.content(content)
			.build();
	}
}
