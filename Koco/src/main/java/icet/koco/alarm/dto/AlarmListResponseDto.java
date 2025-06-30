// AlarmListResponseDto.java
package icet.koco.alarm.dto;

import icet.koco.enums.AlarmType;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AlarmListResponseDto {
    private int totalCount;
    private Long cursorId;       // 마지막 알람 ID
    private boolean hasNext;     // 다음 페이지 존재 여부
    private List<AlarmDto> alarms;

    @Data
    @Builder
    @AllArgsConstructor
    public static class AlarmDto {
        private Long id;
        private Long postId;
        private String postTitle;
        private Long receiverId;
        private Long senderId;
        private String senderNickname;
        private AlarmType alarmType;
//        private String url;
        private LocalDateTime createdAt;
    }
}
