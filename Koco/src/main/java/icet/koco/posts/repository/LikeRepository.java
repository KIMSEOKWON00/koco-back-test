package icet.koco.posts.repository;

import icet.koco.posts.entity.Like;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, Long> {
    Integer countByPostId(Long postId);

    boolean existsByUserIdAndPostId(Long userId, Long postId);

    Optional<Like> findByUserIdAndPostId(Long userId, Long postId);
}
