package icet.koco.posts.repository;

import icet.koco.posts.entity.Post;
import icet.koco.posts.entity.PostCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostCategoryRepository extends JpaRepository<PostCategory, Long> {
    void deleteByPost(Post post);
}
