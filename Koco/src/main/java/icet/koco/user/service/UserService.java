package icet.koco.user.service;

import icet.koco.auth.entity.OAuth;
import icet.koco.auth.repository.OAuthRepository;
import icet.koco.auth.service.KakaoOAuthClient;
import icet.koco.global.exception.ResourceNotFoundException;
import icet.koco.global.exception.UnauthorizedException;
import icet.koco.user.dto.UserAlgorithmStatsResponseDto;
import icet.koco.user.dto.UserInfoResponseDto;
import icet.koco.user.entity.User;
import icet.koco.user.entity.UserAlgorithmStats;
import icet.koco.user.repository.UserAlgorithmStatsRepository;
import icet.koco.user.repository.UserRepository;
import icet.koco.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.redis.core.RedisTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final KakaoOAuthClient kakaoOAuthClient;
    private final RedisTemplate<String, String> redisTemplate;
    private final OAuthRepository oAuthRepository;
    private final CookieUtil cookieUtil;
    private final UserAlgorithmStatsRepository userAlgorithmStatsRepository;

    /**
     * 유저 탈퇴
     * @param userId
     * @param response
     */
    @Transactional
    public void deleteUser(Long userId, HttpServletResponse response) {
        // 1. 유저 조회
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
            .orElseThrow(() -> new ResourceNotFoundException("해당 유저 정보를 찾을 수 없습니다."));

        // 2. OAuth 정보 조회
        OAuth oauth = oAuthRepository.findByUserId(userId)
            .orElseThrow(() -> new UnauthorizedException("OAuth 정보가 존재하지 않습니다."));

        // 3. Kakao unlink 호출
        try {
            kakaoOAuthClient.unlinkUser(oauth.getProviderId());
        } catch (Exception e) {
            log.error(">>>>> Kakao unlink 실패: {}", e.getMessage());
        }

        // 4. Redis refreshToken 삭제
        try {
            redisTemplate.delete(userId.toString());
        } catch (Exception e) {
            log.warn("redis에서 refreshToken 삭제 실패: {}", e.getMessage());
        }

        // 5. Soft delete 처리
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);

        // 6. 쿠키 삭제 처리
        cookieUtil.invalidateCookie(response, "access_token");
        cookieUtil.invalidateCookie(response, "refresh_token");
    }


    /**
     * 유저 정보 등록
     * @param userId
     * @param nickname
     * @param statusMsg
     * @param profileImgUrl
     */
    @Transactional
    public void postUserInfo(Long userId, String nickname, String statusMsg, String profileImgUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("유저를 찾을 수 없습니다."));

        String userName = user.getName();

        user.setNickname(nickname!=null?nickname:userName);         // 혹시 nickname이 안 들어오면 name으로 저장
        user.setProfileImgUrl(profileImgUrl);
        user.setStatusMsg(statusMsg);

        userRepository.save(user);
    }

    /**
     * 유저 정보 수정(업데이트)
     * @param userId
     * @param nickname
     * @param profileImgUrl
     * @param statusMsg
     */
    @Transactional
    public void updateUserInfo(Long userId, String nickname, String statusMsg, String profileImgUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("유저를 찾을 수 없습니다."));

        if (nickname != null) {
            user.setNickname(nickname);
        }

        if (profileImgUrl != null) {
            user.setProfileImgUrl(profileImgUrl);
        }

        if (statusMsg != null) {
            user.setStatusMsg(statusMsg);
        }

        userRepository.save(user);
    }



    /**
     * 유저 정보 조회
     * @param userId
     * @return
     */
    @Transactional(readOnly = true)
    public UserInfoResponseDto getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("유저를 찾을 수 없습니다."));

        return UserInfoResponseDto.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .statusMsg(user.getStatusMsg())
                .profileImgUrl(user.getProfileImgUrl())
                .build();
    }


    /**
     * userAlgorithmStats 조회 API
     * @param userId
     * @return
     */
    @Transactional(readOnly = true)
    public UserAlgorithmStatsResponseDto getAlgorithmStats(Long userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
            .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));

        List<UserAlgorithmStats> stats = userAlgorithmStatsRepository.findByUserId(userId);

        // 정답률 기준 내림차순 정렬 후 상위 5개만 추출
        List<UserAlgorithmStatsResponseDto.CategoryStat> topStats = stats.stream()
            .sorted(Comparator.comparingInt(UserAlgorithmStats::getCorrectRate).reversed())
            .limit(5)
            .map(s -> UserAlgorithmStatsResponseDto.CategoryStat.builder()
                .categoryId(s.getCategory().getId())
                .categoryName(s.getCategory().getName())
                .correctRate(s.getCorrectRate())
                .build())
            .toList();

        return UserAlgorithmStatsResponseDto.builder()
            .studyStats(topStats)
            .build();

    }

}

