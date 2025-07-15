package icet.koco.posts.repository;

import icet.koco.posts.dto.post.TopPostResponseDto;
import icet.koco.posts.entity.Post;

import java.time.LocalDateTime;
import java.util.List;

public interface CustomPostRepository {
    List<Post> searchPosts(List<String> categoryNames, String keyword, Long cursorId, int size);

    List<Post> getMyPosts(Long userId, Long cursorId, int size);

    List<TopPostResponseDto> findTopPostsDtoByLikesLastWeek(LocalDateTime start, LocalDateTime end, int limit);
}
