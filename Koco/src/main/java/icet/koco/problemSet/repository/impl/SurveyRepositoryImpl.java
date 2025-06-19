package icet.koco.problemSet.repository.impl;


import icet.koco.problemSet.repository.SurveyRepositoryCustom;
import icet.koco.user.dto.UserCategoryStatProjection;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SurveyRepositoryImpl implements SurveyRepositoryCustom {

    private final EntityManager em;

    @Override
    public List<UserCategoryStatProjection> calculateCorrectRateByCategory(Long userId) {
        return em.createQuery("""
        SELECT new icet.koco.user.dto.UserCategoryStatDto(
            pc.category.id,
            pc.category.name,
            (SUM(CASE WHEN s.isSolved = true THEN 1 ELSE 0 END) * 1.0) / COUNT(s)
        )
        FROM Survey s
        JOIN ProblemCategory pc ON s.problem.id = pc.problem.id
        WHERE s.user.id = :userId
        GROUP BY pc.category.id, pc.category.name
        ORDER BY ((SUM(CASE WHEN s.isSolved = true THEN 1 ELSE 0 END) * 1.0) / COUNT(s)) DESC
        """, UserCategoryStatProjection.class)
            .setParameter("userId", userId)
            .getResultList();
    }

}
