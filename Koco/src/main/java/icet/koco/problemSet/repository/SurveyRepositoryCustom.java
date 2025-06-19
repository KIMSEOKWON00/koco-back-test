package icet.koco.problemSet.repository;

import icet.koco.user.dto.UserCategoryStatProjection;
import java.util.List;

public interface SurveyRepositoryCustom {
    List<UserCategoryStatProjection> calculateCorrectRateByCategory(Long userId);
}