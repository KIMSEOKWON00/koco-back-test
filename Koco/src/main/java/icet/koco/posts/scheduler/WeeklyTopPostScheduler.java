package icet.koco.posts.scheduler;

import icet.koco.posts.service.WeeklyTopPostCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WeeklyTopPostScheduler {

    private final WeeklyTopPostCacheService cacheService;

    @Scheduled(cron = "0 0 0 * * MON")
    public void updateTopPostsCache() {
        cacheService.getOrGenerateTopPosts();
    }
}