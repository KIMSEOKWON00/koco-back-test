package icet.koco.auth.entity;

import icet.koco.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Builder
@Table(name = "oauth")
public class OAuth {
    // OAuth id
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 회원 ID
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 서비스 제공자가 제공하는 ID
    @Column(name = "provider_id", nullable = false, unique = true, length = 100)
    private String providerId;

    // 서비스 제공자의 종류 (ex. Kakao, Google)
    @Column(name = "provider", nullable = false, length = 20)
    private String provider;

    // 리프레시 토큰 저장
    @Column(name = "refresh_token", length = 512)
    private String refreshToken;

}
