package icet.koco.problemSet.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "problem")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // 백준 문제 번호
    @Column(name = "number", nullable = false)
    private Long number;

    // 문제 티어 (예: "Silver 3", "Gold 5" 등)
    @Column(name = "tier", nullable = false, length = 20)
    private String tier;

    // 문제 제목
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    // 문제 설명
    @Lob
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    // 입력 설명
    @Lob
    @Column(name = "input_description", nullable = false, columnDefinition = "TEXT")
    private String inputDescription;

    // 출력 설명
    @Lob
    @Column(name = "output_description", nullable = false, columnDefinition = "TEXT")
    private String outputDescription;

    // 시간 제한 (ms 단위)
    @Column(name = "time_limit")
    private Integer timeLimit;

    // 메모리 제한 (MB 단위)
    @Column(name = "memory_limit")
    private Integer memoryLimit;

    // 제출 수
    @Column(name = "submission_cnt")
    private Integer submissionCnt;

    // 정답 수
    @Column(name = "answer_cnt")
    private Integer answerCnt;

    // 맞춘 사람 수
    @Column(name = "correct_ppl_cnt")
    private Integer correctPplCnt;

    // 정답률 (소수점 3자리까지)
    @Column(name = "correct_rate", columnDefinition = "DECIMAL(6,3)")
    private Double correctRate;

    // 예제 입력
    @Column(name = "input_example", nullable = false, columnDefinition = "TEXT")
    private String inputExample;

    // 예제 출력
    @Column(name = "output_example", nullable = false, columnDefinition = "TEXT")
    private String outputExample;

    // 백준 문제 링크
    @Column(name = "boj_url", nullable = false, length = 100)
    private String bojUrl;

    // 매핑
    @OneToMany(mappedBy = "problem")
    private List<ProblemSetProblem> problemSetProblems = new ArrayList<>();

    // 매핑
    @OneToMany(mappedBy = "problem")
    private List<ProblemCategory> problemCategories = new ArrayList<>();
}
