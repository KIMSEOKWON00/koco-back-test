package icet.koco.user.entity;

import icet.koco.problemSet.entity.Category;
import icet.koco.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "user_algorithm_stats",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "category_id"})
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserAlgorithmStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 알고리즘 카테고리
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // 정답률 (단위: %)
    @Column(name = "correct_rate", nullable = false)
    private Integer correctRate;
}
