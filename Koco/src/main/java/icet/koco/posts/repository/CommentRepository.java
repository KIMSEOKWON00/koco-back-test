package icet.koco.posts.repository;

import icet.koco.posts.entity.Comment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Integer countByPostIdAndDeletedAtIsNull(Long postId);

    Optional<Comment> findByIdAndDeletedAtIsNull(Long id);

    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.deletedAt IS NULL ORDER BY c.id DESC")
    List<Comment> findTopByPostIdAndDeletedAtIsNullOrderByIdDesc(@Param("postId") Long postId, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.id < :cursorId AND c.deletedAt IS NULL ORDER BY c.id DESC")
    List<Comment> findNextPage(@Param("postId") Long postId, @Param("cursorId") Long cursorId, Pageable pageable);

    default List<Comment> findTopByPostIdAndDeletedAtIsNullOrderByIdDesc(Long postId, int limit) {
        return findTopByPostIdAndDeletedAtIsNullOrderByIdDesc(postId, PageRequest.of(0, limit));
    }

    default List<Comment> findNextPage(Long postId, Long cursorId, int limit) {
        return findNextPage(postId, cursorId, PageRequest.of(0, limit));
    }

}
