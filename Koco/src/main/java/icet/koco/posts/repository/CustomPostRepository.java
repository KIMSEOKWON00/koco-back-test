package icet.koco.posts.repository;

import icet.koco.posts.entity.Post;

import java.util.List;

public interface CustomPostRepository {
    List<Post> searchPosts(List<String> categoryNames, String keyword, Long cursorId, int size);
}
