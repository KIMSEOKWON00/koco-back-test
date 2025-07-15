package icet.koco.fixture;

import icet.koco.problemSet.entity.Category;

public class CategoryFixture {
	public static Category category(Long id, String name) {
		return Category.builder()
			.id(id)
			.name(name)
			.build();
	}
}
