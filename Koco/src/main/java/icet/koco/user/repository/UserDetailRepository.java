package icet.koco.user.repository;

import icet.koco.user.entity.User;
import icet.koco.user.entity.UserDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserDetailRepository extends JpaRepository<UserDetail, Long> {

    // 유저 ID로 상세 정보 조회
    Optional<UserDetail> findByUser(User user);
}
