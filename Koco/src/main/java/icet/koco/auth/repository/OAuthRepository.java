package icet.koco.auth.repository;

import icet.koco.auth.entity.OAuth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface OAuthRepository extends JpaRepository<OAuth, Long> {

    // providerId로 OAuth 정보 조회
    Optional<OAuth> findByProviderId(String providerId);

    // provider + providerId로 정확히 식별
    Optional<OAuth> findByProviderAndProviderId(String provider, String providerId);

    // user_id로 OAuth 정보 조회
    Optional<OAuth> findByUserId(Long userId);

    // 사용자 ID로 refresh token을 갱신합니다.
    @Transactional
    @Modifying
    @Query("UPDATE OAuth o SET o.refreshToken = :refreshToken WHERE o.user.id = :userId")
    void updateRefreshToken(Long userId, String refreshToken);

}
