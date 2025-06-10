package icet.koco.problemSet.repository;

import icet.koco.problemSet.entity.Problem;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProblemRepository extends JpaRepository<Problem, Long> {

    // 백준 문제 번호로
    Optional<Problem> findByNumber(Long Number);

}
