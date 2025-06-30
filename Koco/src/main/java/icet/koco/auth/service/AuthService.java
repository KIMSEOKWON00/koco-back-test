package icet.koco.auth.service;

import icet.koco.auth.dto.AuthResponse;
import icet.koco.auth.dto.KakaoUserResponse;
import icet.koco.auth.dto.LogoutResponse;
import icet.koco.auth.dto.RefreshResponse;
import icet.koco.auth.entity.OAuth;
import icet.koco.auth.repository.OAuthRepository;
import icet.koco.enums.ErrorMessage;
import icet.koco.global.exception.UnauthorizedException;
import icet.koco.user.entity.User;
import icet.koco.user.repository.UserRepository;
import icet.koco.util.CookieUtil;
import icet.koco.util.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final KakaoOAuthClient kakaoOAuthClient;
    private final UserRepository userRepository;
    private final OAuthRepository oauthRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;
    private final CookieUtil cookieUtil;

    /**
     * 카카오 로그인
     * @param code (인가코드)
     * @param response
     * @return
     */
    public AuthResponse loginWithKakao(String code, HttpServletResponse response) {
        // 인가코드 이용하여 Kakao 정보 받아오기
        KakaoUserResponse kakaoUser = kakaoOAuthClient.getUserInfo(code);

        // DB에 유저 이메일이 저장되었는지를 기준으로 신규 / 기존 유저 판별
        Optional<User> userOpt = userRepository.findByEmail(kakaoUser.getEmail());

        // 기존 사용자
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // 탈퇴 사용자면 deletedAt만 null로 하고 복구처리
            if (user.getDeletedAt() != null) {
                user.setDeletedAt(null);
                userRepository.save(user);
            }

            // RefreshToken Redis에 저장
            String refreshToken = jwtTokenProvider.createRefreshToken(user);
            System.out.println("refreshToken: " + refreshToken);
            try {
                redisTemplate.opsForValue().set(user.getId().toString(), refreshToken);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // RefreshToken DB에 업데이트
            oauthRepository.updateRefreshToken(user.getId(), refreshToken);

            // RefreshToken 쿠키로 전달
            Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
            refreshCookie.setHttpOnly(true);
            refreshCookie.setMaxAge(7 * 24 * 60 * 60);
            refreshCookie.setSecure(true);
            refreshCookie.setPath("/");
            response.addCookie(refreshCookie);

            // accessToken 발급
            String accessToken = jwtTokenProvider.createAccessToken(user);
            System.out.println("accessToken: " + accessToken);

            // accessToken 쿠키로 전달
            Cookie accessTokenCookie = new Cookie("access_token", accessToken);
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setMaxAge(30 * 60);      // 30분
            accessTokenCookie.setPath("/");
            response.addCookie(accessTokenCookie);

            // 응답 반환
            return AuthResponse.builder()
                .code("LOGIN_SUCCESS")
                .message("로그인 성공. 토큰 발급 완료")
                .data(AuthResponse.AuthData.builder()
                    .email(user.getEmail())
                    .name(user.getName())
                    .isRegistered(true)
                    .build())
                .build();
        }

        // 신규 사용자일 경우
        try {
            // 사용자 내용 DB에 저장
            User newUser = userRepository.save(User.builder()
                .email(kakaoUser.getEmail())
                .name(kakaoUser.getName())
                .nickname(kakaoUser.getName())
                .createdAt(LocalDateTime.now())
                .build());

            // 리프레시 토큰 발급
            String refreshToken = jwtTokenProvider.createRefreshToken(newUser);

            // 액세스 토큰 발급
            String accessToken = jwtTokenProvider.createAccessToken(newUser);

            // Redis에 리프레시 토큰 저장
            try {
                redisTemplate.opsForValue().set(newUser.getId().toString(), refreshToken);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // oauth DB에 저장
            oauthRepository.save(OAuth.builder()
                .provider("kakao")
                .providerId(kakaoUser.getProviderId())
                .user(newUser)
                .refreshToken(refreshToken)
                .build());

            // RefreshToken 쿠키로 전달
            Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
            refreshCookie.setHttpOnly(true);
            refreshCookie.setMaxAge(7 * 24 * 60 * 60);
            refreshCookie.setSecure(true);
            refreshCookie.setPath("/");
            response.addCookie(refreshCookie);

            // accessToken 쿠키로 전달
            Cookie accessTokenCookie = new Cookie("access_token", accessToken);
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setMaxAge(30 * 60);      // 30분
            accessTokenCookie.setPath("/");
            response.addCookie(accessTokenCookie);

            return AuthResponse.builder()
                .code("LOGIN_SUCCESS")
                .message("로그인 성공. 토큰 발급 완료")
                .data(AuthResponse.AuthData.builder()
                    .email(newUser.getEmail())
                    .name(newUser.getName())
                    .isRegistered(false)
                    .build())
                .build();
        } catch (Exception e) {
            log.error("신규 가입 처리 중 예외 발생: {}", e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("회원 가입 중 에러 발생");
        }
    }

    /**
     * 로그아웃
     * @param request
     * @param response
     * @return
     */
    public LogoutResponse logout(HttpServletRequest request, HttpServletResponse response) {
        // 쿠키에서 accessToken 추출
        String accessToken = extractTokenFromCookies(request);

        // 유효성 검사
        if (accessToken == null || !jwtTokenProvider.isInvalidToken(accessToken)) {
            if (accessToken == null) {
                log.info(">>>>> (LogoutServie) Invalid access token");
            }
            throw new UnauthorizedException(ErrorMessage.INVALID_ACCESS_TOKEN);
        }

        Long userId;
        // 3. 토큰에서 userId 추출
        try {
            userId = jwtTokenProvider.getUserIdFromToken(accessToken);
        } catch (Exception e) {
            throw new UnauthorizedException(ErrorMessage.USER_ID_EXTRACTION_FAILED);
        }


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

    /**
     * 리프레시 토큰 갱신
     * @param refreshToken
     * @param response
     * @return
     */
    public RefreshResponse refreshAccessToken(String refreshToken, HttpServletResponse response) {
        if (!jwtTokenProvider.isInvalidToken(refreshToken)) {
            throw new UnauthorizedException(ErrorMessage.INVALID_REFRESH_TOKEN);
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        Optional<OAuth> oauthOpt = Optional.ofNullable(oauthRepository.findByUserId(userId)
                .orElseThrow(() -> new UnauthorizedException(ErrorMessage.OAUTH_NOT_FOUND)));

        String redisToken = redisTemplate.opsForValue().get(userId.toString());

        if (redisToken == null || !redisToken.equals(refreshToken)) {
            throw new UnauthorizedException(ErrorMessage.REDIS_NOT_MATCH);
        }

        if (oauthOpt.isEmpty() || !refreshToken.equals(oauthOpt.get().getRefreshToken())) {
            throw new UnauthorizedException(ErrorMessage.DB_NOT_MATCH);
        }

        User user = oauthOpt.get().getUser();
        String newAccessToken = jwtTokenProvider.createAccessToken(user);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user);

        // redis에 새 refreshToken 저장
        redisTemplate.opsForValue().set(user.getId().toString(), newRefreshToken);

        // DB에 새 refreshToken 저장
        oauthRepository.updateRefreshToken(user.getId(), newRefreshToken);

        // accessToken 쿠키로 전달
        Cookie accessCookie = new Cookie("access_token", newAccessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(30 * 60);
        response.addCookie(accessCookie);

        // RefreshToken 쿠키로 전달
        Cookie refreshCookie = new Cookie("refresh_token", newRefreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(refreshCookie);

        // 응답 반환
        return RefreshResponse.builder()
            .code("TOKEN_REFRESH_SUCCESS")
            .message("토큰이 성공적으로 재발급되었습니다.")
            .build();
    }

    /**
     * 쿠키에서 토큰 추출
     * @param request
     * @return
     */
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

