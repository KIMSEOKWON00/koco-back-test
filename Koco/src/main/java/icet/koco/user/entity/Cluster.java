package icet.koco.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cluster")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Cluster {
    // id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // 알고리즘 이름
    @Column(name = "name", nullable = false, length = 20)
    private String name;
}
