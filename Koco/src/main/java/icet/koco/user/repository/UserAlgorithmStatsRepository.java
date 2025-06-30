package icet.koco.user.repository;

import icet.koco.user.entity.UserAlgorithmStats;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserAlgorithmStatsRepository extends JpaRepository<UserAlgorithmStats, Long> {
    List<UserAlgorithmStats> findByUserId(Long userId);

    // 사용자 Id로 작성된 알고리즘 스탯 삭제
    @Modifying
    @Query("DELETE FROM UserAlgorithmStats u WHERE u.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
