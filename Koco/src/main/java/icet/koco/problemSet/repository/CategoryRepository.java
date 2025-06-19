package icet.koco.problemSet.repository;

import icet.koco.problemSet.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

}
