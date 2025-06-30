package icet.koco.problemSet.repository;

import icet.koco.problemSet.entity.Problem;
import icet.koco.problemSet.entity.ProblemSet;

import icet.koco.problemSet.entity.ProblemSetProblem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProblemSetProblemRepository extends JpaRepository<ProblemSetProblem, Long> {

    // 문제집 ID로 문제 번호(ID)만 조회
    @Query("""
        SELECT p.id
        FROM ProblemSetProblem psp
        JOIN psp.problem p
        WHERE psp.problemSet.id = :problemSetId
    """)
    List<Long> findProblemIdsByProblemSetId(@Param("problemSetId") Long problemSetId);

    // 문제집(ProblemSet)에 포함된 문제 전체 조회
    List<ProblemSetProblem> findAllByProblemSet(ProblemSet problemSet);

    boolean existsByProblemSetIdAndProblemId(Long problemSetId, Long problemId);

    // ProblemSetProblemRepository.java
    @Query("SELECT psp FROM ProblemSetProblem psp JOIN FETCH psp.problem WHERE psp.problemSet = :problemSet")
    List<ProblemSetProblem> findWithProblemsByProblemSet(@Param("problemSet") ProblemSet problemSet);

    @Query("SELECT p.problem.id FROM ProblemSetProblem p WHERE p.problemSet.id = :problemSetId AND p.problem.id IN :problemIds")
    List<Long> findIncludedProblemIds(@Param("problemSetId") Long problemSetId, @Param("problemIds") List<Long> problemIds);

	boolean existsByProblemSetAndProblem(ProblemSet problemSet, Problem problem);


}
