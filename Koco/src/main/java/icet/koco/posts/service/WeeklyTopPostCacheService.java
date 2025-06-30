package icet.koco.posts.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import icet.koco.posts.dto.post.TopPostResponseDto;
import icet.koco.posts.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyTopPostCacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final PostRepository postRepository;
    private final ObjectMapper objectMapper;

    private static final String REDIS_KEY_PREFIX = "top_posts:week:";

    /**
     * 인기 게시글 가져오기
     * @return List<TopPostResponseDto>
     */
    public List<TopPostResponseDto> getOrGenerateTopPosts() {
        // 이번 주의 캐시 키 만들기
        String key = getCurrentWeekKey();

        // 해당 키로 인기 게시글 가져오기
        List<TopPostResponseDto> cached = getFromRedis(key);

        // Redis에 저장되어 있으면 해당 게시글 리스트 사용
        if (cached != null && !cached.isEmpty()) {
            return cached;
        } else {
            // 없다면 직접 계산
            List<TopPostResponseDto> topPosts = calculateTopPostsOfLastWeek();

            // 가져온 데이터를 json으로 저장해서 Redis에 저장
            try {
                String json = objectMapper.writeValueAsString(topPosts);
                redisTemplate.opsForValue().set(key, json, Duration.ofDays(7));
            } catch (Exception e) {
                log.error(">>>>> Redis 저장 실패: {}", e.getMessage());
            }
            return topPosts;
        }
    }

    /**
     * Redis에서 인기 게시글 가져오기
     * @param key 캐시 키
     * @return
     */
    private List<TopPostResponseDto> getFromRedis(String key) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json != null && !json.isEmpty()) {
                return objectMapper.readValue(json, new TypeReference<>() {
                });
            }
        } catch (Exception e) {
            log.error(">>>>> Redis 파싱 실패: {}",e.getMessage());
        }
        return null;
    }


    /**
     * DB에서 저번 주 인기 게시글 5개 조회
     * @return
     */
    private List<TopPostResponseDto> calculateTopPostsOfLastWeek() {
        // 날짜 계산
        LocalDateTime start = LocalDate.now().minusWeeks(1).with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime end = LocalDate.now().minusWeeks(1).with(DayOfWeek.SUNDAY).atTime(23, 59, 59);

        // 저번주 동안 좋아요 수가 많은 상위 5개 게시글 조회
        List<TopPostResponseDto> topPosts = postRepository.findTopPostsDtoByLikesLastWeek(start, end, 5);

        return topPosts;
    }

    /**
     * Redis에서 사용할 키 생성
     * @return
     */
    private String getCurrentWeekKey() {
        // 지난 주 월요일 값이 키
        LocalDate monday = LocalDate.now().minusWeeks(1).with(DayOfWeek.MONDAY);
        return REDIS_KEY_PREFIX + monday;
    }

}
