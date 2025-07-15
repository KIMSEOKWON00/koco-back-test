package icet.koco.problemSet.service;

import icet.koco.problemSet.dto.PreviousProblemListResponseDto;
import icet.koco.problemSet.repository.ProblemSetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProblemService {

	private final ProblemSetRepository problemSetRepository;

	// 주말이면 가장 최근 평일(금요일)로 조정
	private LocalDate getPreviousIssueDate() {
		LocalDate date = LocalDate.now().minusDays(1);
		DayOfWeek day = date.getDayOfWeek();

		if (day == DayOfWeek.SUNDAY) return date.minusDays(2); // 금요일
		if (day == DayOfWeek.SATURDAY) return date.minusDays(1); // 금요일
		return date;
	}

	public PreviousProblemListResponseDto getPreviousProblemInfo() {
		LocalDate previousDay = getPreviousIssueDate();

		List<Long> ids = problemSetRepository.findProblemIdsByProblemSetCreatedAt(previousDay);

		if (!ids.isEmpty()) {
			return new PreviousProblemListResponseDto(ids, previousDay);
		}

		// 전날에 출제된 문제가 없으면 가장 최신 문제집으로
		LocalDate latestDate = problemSetRepository.findLatestProblemSetDate();
		List<Long> fallbackIds = problemSetRepository.findProblemIdsByProblemSetCreatedAt(latestDate);

		return new PreviousProblemListResponseDto(fallbackIds, latestDate);
	}
}




