package icet.koco.user.repository;

import icet.koco.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    // 이름으로 유저 조회
    Optional<User> findByName(String name);

    // 닉네임으로 유저 조회
    Optional<User> findByNickname(String nickname);

    // 이메일으로 유저 조회
    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);

    // ID로 조회하면서 soft delete 제외
    Optional<User> findByIdAndDeletedAtIsNull(Long id);

    // 삭제되지 않은 유저 리스트 전체 조회
    List<User> findAllByDeletedAtIsNull();

    // 이름 기준 정렬해서 전체 조회
    List<User> findAllByDeletedAtIsNullOrderByNameAsc();

    // 닉네임 기준 정렬해서 전체 조회
    List<User> findAllByDeletedAtIsNullOrderByNicknameAsc();

}

