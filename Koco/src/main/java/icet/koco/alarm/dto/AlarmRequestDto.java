package icet.koco.alarm.dto;

import icet.koco.enums.AlarmType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlarmRequestDto {
    private Long postId;
    private Long senderId;
    private AlarmType alarmType;    // 'COMMENT' or 'LIKE'
}
