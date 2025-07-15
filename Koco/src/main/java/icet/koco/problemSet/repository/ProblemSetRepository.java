package icet.koco.problemSet.repository;

import icet.koco.problemSet.entity.ProblemSet;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProblemSetRepository extends JpaRepository<ProblemSet, Long> {
    // 특정 날짜에 생성된 문제집 조회
    Optional<ProblemSet> findByCreatedAt(LocalDate createdAt);


//	@Query("SELECT psp.problem.id FROM ProblemSetProblem psp " +
//		"WHERE psp.problemSet.createdAt = :date")
//	List<Long> findProblemIdsByProblemSetCreatedAt(@Param("date") LocalDate date);

	@Query("""
    SELECT psp.problem.id
    FROM ProblemSetProblem psp
    WHERE psp.problemSet.createdAt = (
        SELECT MAX(ps.createdAt) FROM ProblemSet ps
    )
""")
	List<Long> findProblemIdsFromLatestProblemSet();

	@Query("SELECT psp.problem.id FROM ProblemSetProblem psp WHERE psp.problemSet.createdAt = :date")
	List<Long> findProblemIdsByProblemSetCreatedAt(@Param("date") LocalDate date);

	@Query("SELECT MAX(ps.createdAt) FROM ProblemSet ps")
	LocalDate findLatestProblemSetDate();

}
