package icet.koco.problemSet.repository;

import icet.koco.problemSet.entity.ProblemSet;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProblemSetRepository extends JpaRepository<ProblemSet, Long> {
    // 특정 날짜에 생성된 문제집 조회
    Optional<ProblemSet> findByCreatedAt(LocalDate createdAt);

}
