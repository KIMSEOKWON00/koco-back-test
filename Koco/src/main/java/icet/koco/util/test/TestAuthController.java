package icet.koco.util.test;


import icet.koco.user.entity.User;
import icet.koco.user.repository.UserRepository;
import icet.koco.util.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/backend/test")
@Tag(name = "Test", description = "테스트용 API입니다.")
public class TestAuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @PostMapping("/token")
    @Operation(summary = "테스트용 access_token을 발급하게 하는 API입니다. ")
    public ResponseEntity<?> createTestToken(@RequestParam Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        String token = jwtTokenProvider.createAccessToken(user);
        System.out.println("testAuth token: " + token);
        return ResponseEntity.ok(Map.of("access_token", token));
    }

    @GetMapping("/timezone")
    @Operation(summary = "타임존 확인용 API입니다.")
    public ResponseEntity<Map<String, Object>> getCurrentServerTime() {
        Map<String, Object> result = new HashMap<>();

        result.put("localDateTime", LocalDateTime.now()); // 시간대 없음
        result.put("zonedDateTime", ZonedDateTime.now()); // 시간대 포함
        result.put("offsetDateTime", OffsetDateTime.now()); // 시간대 + 오프셋
        result.put("jvmTimeZone", TimeZone.getDefault().getID()); // JVM 타임존

        return ResponseEntity.ok(result);
    }
}
