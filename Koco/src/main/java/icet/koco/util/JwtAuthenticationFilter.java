package icet.koco.util;

import icet.koco.util.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, RedisTemplate<String, String> redisTemplate) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().equals("/api/backend/v1/auth/refresh");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain)
        throws ServletException, IOException {

        String token = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("access_token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token != null) {
            System.out.println(">>>>> access_token 추출됨: " + token);

            // 토큰 유효성 검사 실패
            if (!jwtTokenProvider.validateToken(token)) {
                System.out.println(">>>>> 토큰 유효성 검사 실패");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"expired token\"}");
                response.getWriter().flush();
                return;
            }

            // 블랙리스트 검사
            String isBlacklisted = redisTemplate.opsForValue().get("BL:" + token);
            if (isBlacklisted != null) {
                System.out.println(">>>>> 블랙리스트 토큰");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"BL checked. expired token\"}");
                response.getWriter().flush();
                return;
            }

            // 정상 토큰 → userId 추출 및 인증 객체 생성
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            System.out.println(">>>>> userId 추출됨: " + userId);

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userId,
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            System.out.println(">>>>> SecurityContext에 인증 정보 저장 완료");
        }

        // 다음 필터로 진행
        filterChain.doFilter(request, response);
    }
}
