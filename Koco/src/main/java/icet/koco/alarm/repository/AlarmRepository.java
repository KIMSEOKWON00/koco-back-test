package icet.koco.alarm.repository;

import icet.koco.alarm.entity.Alarm;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AlarmRepository extends JpaRepository<Alarm, Long>, AlarmRepositoryCustom {
    List<Alarm> findByReceiverIdAndIsReadFalse(Long receiverId);

    Integer countByReceiverIdAndIsReadFalse(Long receiverId);

}