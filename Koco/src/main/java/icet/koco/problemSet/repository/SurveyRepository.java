package icet.koco.problemSet.repository;

import icet.koco.problemSet.entity.Survey;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SurveyRepository extends JpaRepository<Survey, Long> {
    // 설문 작성된 문제 ID 목록 조회
    @Query("SELECT s.problem.id FROM Survey s WHERE s.user.id = :userId AND s.problemSet.id = :problemSetId")
    List<Long> findProblemIdsByUserAndProblemSet(@Param("userId") Long userId, @Param("problemSetId") Long problemSetId);

    List<Survey> findByUserId(Long userId);

}
