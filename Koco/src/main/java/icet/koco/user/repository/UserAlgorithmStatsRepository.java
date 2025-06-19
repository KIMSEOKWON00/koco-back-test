package icet.koco.user.repository;

import icet.koco.user.entity.User;
import icet.koco.user.entity.UserAlgorithmStats;
import icet.koco.problemSet.entity.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserAlgorithmStatsRepository extends JpaRepository<UserAlgorithmStats, Long> {

    // 특정 사용자에 대한 전체 알고리즘 통계 조회
    List<UserAlgorithmStats> findByUser(User user);

    // 특정 사용자 + 카테고리 통계 조회
    Optional<UserAlgorithmStats> findByUserIdAndCategoryId(Long userId, Long categoryId);

    Optional<UserAlgorithmStats> findByUserAndCategory(User user, Category category);

    List<UserAlgorithmStats> findByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM UserAlgorithmStats u WHERE u.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
