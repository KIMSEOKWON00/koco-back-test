package icet.koco.problemSet.repository;

import icet.koco.problemSet.entity.Problem;
import icet.koco.problemSet.entity.Solution;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolutionRepository extends JpaRepository<Solution, Long> {
    // 중복 여부 확인용
    boolean existsByProblem(Problem problem);

    Optional<Solution> findByProblem(Problem problem);

    List<Solution> findByProblemIdIn(List<Long> problemIds);
}

