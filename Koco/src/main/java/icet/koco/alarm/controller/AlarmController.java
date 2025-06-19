package icet.koco.alarm.controller;

import icet.koco.alarm.dto.AlarmListResponseDto;
import icet.koco.alarm.dto.AlarmRequestDto;
import icet.koco.alarm.emitter.EmitterRepository;
import icet.koco.alarm.repository.AlarmRepository;
import icet.koco.alarm.service.AlarmService;

import icet.koco.enums.ApiResponseCode;
import icet.koco.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/backend/v3/alarms")
@Tag(name = "Alarm", description = "알림 관련 API입니다.")
@RequiredArgsConstructor
public class AlarmController {
    private final AlarmService alarmService;
    private final EmitterRepository emitterRepository;
    private final AlarmRepository alarmRepository;

    @PostMapping
    @Operation(summary = "내부에서 알림 생성하는 API입니다. (좋아요 / 댓글)")
    public ResponseEntity<?> createAlarmInternal(@RequestBody AlarmRequestDto requestDto) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        alarmService.createAlarmInternal(requestDto);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/subscribe")
    @Operation(summary = "SSE Emitter 구독하는 API입니다.")
    public SseEmitter subscribe(@RequestHeader(value = "Last-Event-ID", required = false) String lastEventId) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return alarmService.subscribe(userId, lastEventId);
    }

    @GetMapping
    @Operation(summary = "읽지 않은 알림 목록 조회하는 API입니다.")
    public ResponseEntity<?> getAlarms(
            @RequestParam(required = false) Long cursorId,
            @RequestParam(required = false, defaultValue = "10") int size)  {

        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        AlarmListResponseDto responseDto = alarmService.getAlarmList(userId, cursorId, size);

        return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.SUCCESS, "알림 리스트 조회에 성공하였습니다.", responseDto));
    }

    @DeleteMapping("/{alarmId}")
    @Operation(summary = "알림을 삭제하는 API입니다.")
    public ResponseEntity<?> deleteAlarm(@PathVariable("alarmId") Long alarmId) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        alarmService.deleteAlarm(userId, alarmId);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{alarmId}/read")
    @Operation(summary = "알림을 읽는 API입니다.")
    public ResponseEntity<?> readAlarm(@PathVariable("alarmId") Long alarmId) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        alarmService.readAlarm(userId, alarmId);

        return ResponseEntity.noContent().build();
    }

}
