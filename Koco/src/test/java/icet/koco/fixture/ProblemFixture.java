package icet.koco.fixture;

import icet.koco.problemSet.entity.Problem;

public class ProblemFixture {
	public static Problem problem(Long id, Long number) {
		return Problem.builder()
			.id(id)
			.number(number)
			.build();
	}
}
