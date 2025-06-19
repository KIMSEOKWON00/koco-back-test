package icet.koco.problemSet.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import icet.koco.enums.DifficultyLevel;
import icet.koco.user.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "survey", uniqueConstraints = {
    @UniqueConstraint(
        name = "uq_user_problemset_problem",
        columnNames = {"user_id", "problem_set_id", "problem_id"}
    )
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Survey {

    // id (PK)
    @Id
    @Column(name = "id", unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //사용자 ID (users 테이블 FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 출제집 ID (problemSet 테이블 PK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_set_id", nullable = false)
    private ProblemSet problemSet;

    // 문제 ID (problem 테이블 PK)
    @ManyToOne
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    // 해결 여부
    @JsonProperty("isSolved")
    @Column(name = "is_solved", nullable = false)
    private boolean isSolved;

    // 난이도
    @Column(name = "difficulty_level", nullable = false)
    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel;

    @Column(name = "answered_at", nullable = false)
    private LocalDateTime answeredAt;
}
