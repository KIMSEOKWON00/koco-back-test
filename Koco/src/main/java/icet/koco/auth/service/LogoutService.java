package icet.koco.auth.service;

import icet.koco.auth.dto.LogoutResponse;
import icet.koco.auth.repository.OAuthRepository;
import icet.koco.global.exception.UnauthorizedException;
import icet.koco.util.CookieUtil;
import icet.koco.util.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutService {

    private final OAuthRepository oauthRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;
    private final CookieUtil cookieUtil;

    public LogoutResponse logout(HttpServletRequest request, HttpServletResponse response) {
        // 1. 쿠키에서 accessToken 추출
        String accessToken = extractTokenFromCookies(request);
        System.out.println(">>>>> (LogoutService) accessToken: " + accessToken);

        // 2. 유효성 검사
        if (accessToken == null || !jwtTokenProvider.validateToken(accessToken)) {
            if (accessToken == null) {
                System.out.println(">>>>> (LogoutServie) Invalid access token");
            }
            throw new UnauthorizedException("유효하지 않은 토큰입니다.");
        }

        // 3. 토큰에서 userId 추출
        Long userId = jwtTokenProvider.getUserIdFromToken(accessToken);
        System.out.println("userId = " + userId);

        // 4. DB에서 RefreshToken null로 만들기
        oauthRepository.findByUserId(userId).ifPresent(oAuth -> {
            oAuth.setRefreshToken(null);
            oauthRepository.save(oAuth);
        });

        // 5. Redis에서도 삭제
        redisTemplate.delete(userId.toString());

        long expiration = jwtTokenProvider.getExpiration(accessToken);
        redisTemplate.opsForValue().set("BL:" + accessToken, "logout", expiration, java.util.concurrent.TimeUnit.MILLISECONDS);

        // 6. 쿠키 제거
        cookieUtil.invalidateCookie(response, "access_token");
        cookieUtil.invalidateCookie(response, "refresh_token");

        // 7. 응답 반환
        return LogoutResponse.builder()
            .code("LOGOUT_SUCCESS")
            .message("로그아웃 성공.")
            .redirectUrl("/api/v1/auth/login/") // To-Do: 프론트 도메인 나중에 연결
            .build();
    }

    private String extractTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if ("access_token".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
