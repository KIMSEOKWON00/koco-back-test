package icet.koco.problemSet.repository;

import icet.koco.problemSet.entity.ProblemCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProblemCategoryRepository extends JpaRepository<ProblemCategory, Long> {

}
