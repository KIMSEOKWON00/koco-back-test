package icet.koco.auth.service;

import icet.koco.auth.dto.AuthResponse;
import icet.koco.auth.dto.KakaoUserResponse;
import icet.koco.auth.dto.RefreshResponse;
import icet.koco.auth.entity.OAuth;
import icet.koco.auth.repository.OAuthRepository;
import icet.koco.global.exception.UnauthorizedException;
import icet.koco.user.entity.User;
import icet.koco.user.repository.UserRepository;
import icet.koco.util.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final KakaoOAuthClient kakaoOAuthClient;
    private final UserRepository userRepository;
    private final OAuthRepository oauthRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

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
            refreshCookie.setPath("/");
            response.addCookie(refreshCookie);

            // accessToken 발급
            String accessToken = jwtTokenProvider.createAccessToken(user);

            // accessToken 쿠키로 전달
            Cookie cookie = new Cookie("access_token", accessToken);
            cookie.setHttpOnly(true);
            cookie.setMaxAge(30 * 60);
            cookie.setPath("/");
            response.addCookie(cookie);

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

        try {
            User newUser = userRepository.save(User.builder()
                .email(kakaoUser.getEmail())
                .name(kakaoUser.getName())
                .nickname(kakaoUser.getName())
                .createdAt(LocalDateTime.now())
                .build());
            System.out.println(">>> 신규 유저 DB 저장 완료: " + newUser.getId());

            String refreshToken = jwtTokenProvider.createRefreshToken(newUser);
            String accessToken = jwtTokenProvider.createAccessToken(newUser);

            try {
                redisTemplate.opsForValue().set(newUser.getId().toString(), refreshToken);
                System.out.println(">>> Redis 저장 완료 (신규)");
            } catch (Exception e) {
                System.out.println("❗ Redis 저장 실패 (신규가입): " + e.getMessage());
                e.printStackTrace();
            }

            oauthRepository.save(OAuth.builder()
                .provider("kakao")
                .providerId(kakaoUser.getProviderId())
                .user(newUser)
                .refreshToken(refreshToken)
                .build());
            System.out.println(">>> OAuth 저장 완료");

            Cookie accessCookie = new Cookie("access_token", accessToken);
            accessCookie.setHttpOnly(true);
            accessCookie.setMaxAge(30 * 60);
            accessCookie.setPath("/");
            response.addCookie(accessCookie);

            Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
            refreshCookie.setHttpOnly(true);
            refreshCookie.setMaxAge(7 * 24 * 60 * 60);
            refreshCookie.setPath("/");
            response.addCookie(refreshCookie);

            System.out.println(">>> 쿠키 설정 완료");

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
            System.out.println("❗❗ 신규 가입 처리 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("회원 가입 중 에러 발생");
        }
    }

        public RefreshResponse refreshAccessToken(String refreshToken, HttpServletResponse response) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("유효하지 않은 리프레시 토큰입니다.");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        Optional<OAuth> oauthOpt = oauthRepository.findByUserId(userId);

        String redisToken = null;
        try {
            redisToken = redisTemplate.opsForValue().get(userId.toString());
        } catch (Exception e) {
            System.out.println("❗ Redis 조회 실패 (refresh): " + e.getMessage());
        }

        if (redisToken == null || !redisToken.equals(refreshToken)) {
            throw new UnauthorizedException("Redis에 저장된 토큰이 일치하지 않습니다.");
        }

        if (oauthOpt.isEmpty() || !refreshToken.equals(oauthOpt.get().getRefreshToken())) {
            throw new UnauthorizedException("DB에 저장된 토큰이 일치하지 않습니다.");
        }

        User user = oauthOpt.get().getUser();
        String newAccessToken = jwtTokenProvider.createAccessToken(user);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user);

        System.out.println(">>>>> (AuthService: refreshAccessToken) Access token: " + newAccessToken);
        System.out.println(">>>>> (AuthService: refreshAccessToken) Refresh token: " + newRefreshToken);

        try {
            redisTemplate.opsForValue().set(user.getId().toString(), newRefreshToken);
        } catch (Exception e) {
            System.out.println("❗ Redis 저장 실패 (refresh): " + e.getMessage());
        }

        oauthRepository.updateRefreshToken(user.getId(), newRefreshToken);

        Cookie accessCookie = new Cookie("access_token", newAccessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(30 * 60);
        response.addCookie(accessCookie);

        Cookie refreshCookie = new Cookie("refresh_token", newRefreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(refreshCookie);

        return RefreshResponse.builder()
            .code("TOKEN_REFRESH_SUCCESS")
            .message("토큰이 성공적으로 재발급되었습니다.")
            .build();
    }
}

