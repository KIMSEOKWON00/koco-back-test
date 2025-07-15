package icet.koco.alarm.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import icet.koco.alarm.entity.Alarm;
import icet.koco.alarm.entity.QAlarm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AlarmRepositoryImpl implements AlarmRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Alarm> findByReceiverIdWithCursor(Long receiverId, Long cursorId, int size) {
        QAlarm alarm = QAlarm.alarm;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(alarm.receiver.id.eq(receiverId));

        if (cursorId != null) {
            builder.and(alarm.id.lt(cursorId));
        }

        return queryFactory
            .selectFrom(alarm)
            .where(builder)
            .orderBy(alarm.id.desc())
            .limit(size + 1)
            .fetch();
    }

    @Override
    public List<Alarm> findByReceiverIdWithCursorAndIsReadFalse(Long receiverId, Long cursorId, int size) {
        QAlarm alarm = QAlarm.alarm;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(alarm.receiver.id.eq(receiverId));
        builder.and(alarm.isRead.eq(false));

        if (cursorId != null) {
            builder.and(alarm.id.lt(cursorId));
        }

        return queryFactory
            .selectFrom(alarm)
            .where(builder)
            .orderBy(alarm.id.desc())
            .limit(size + 1)
            .fetch();
    }
}
