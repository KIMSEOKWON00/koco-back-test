package icet.koco.problemSet.repository;

import icet.koco.problemSet.entity.ProblemSetSolution;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProblemSetSolutionRespository extends JpaRepository<ProblemSetSolution, Long> {
    ProblemSetSolution findBySolutionId(Long solutionId);

    // 문제집 ID로 해설 번호(ID)만 조회
    @Query("""
        SELECT sol.id
        FROM ProblemSetSolution pss
        JOIN pss.solution sol
        WHERE pss.problemSet.id = :problemSetId
    """)
    List<Long> findSolutionIdsByProblemSetId(@Param("problemSetId") Long problemSetId);


}
