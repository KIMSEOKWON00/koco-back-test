package icet.koco.problemSet.repository;

import icet.koco.problemSet.entity.Category;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByNameIn(List<String> names);
}
