package icet.koco.problemRecommendation.repository;

import icet.koco.problemRecommendation.entity.ProblemRecommendation;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProblemRecommendationRepository extends JpaRepository<ProblemRecommendation, Long> {
	@Query("SELECT r FROM ProblemRecommendation r WHERE DATE(r.createdAt) = :date")
	List<ProblemRecommendation> findAllByCreatedAtDate(@Param("date") LocalDate date);
}
