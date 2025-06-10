package icet.koco.problemSet.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "problem_set")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProblemSet {
    // 출제집 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // 출제 날짜
    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    // 이 문제집(ProblemSet)에 포함된 모든 문제(Problem)들과의 연결
    @OneToMany(mappedBy = "problemSet")
    private List<ProblemSetProblem> problemSetProblems = new ArrayList<>();

    // 이 문제집(ProblemSet)에 포함된 모든 해설(Solution)들과의 연결
    @OneToMany(mappedBy = "problemSet")
    private List<ProblemSetSolution> problemSetSolutions = new ArrayList<>();
}