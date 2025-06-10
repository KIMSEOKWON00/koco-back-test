package icet.koco.util;

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

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, RedisTemplate<String, String> redisTemplate) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().equals("/api/backend/v1/auth/refresh")
            || request.getRequestURI().equals("/api/backend/v1/auth/callback")
            || request.getRequestURI().equals("/swagger-ui/index.html#/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain)
        throws ServletException, IOException {

        String token = resolveToken(request);

        // 토큰이 없으면 401 바로 반환
        if (token == null) {
            handleUnauthorized(response, "TOKEN_NOT_FOUND", "Access Token이 존재하지 않습니다.");
            return;
        }

        try {
            // 유효성 검사 실패
            if (!jwtTokenProvider.isInvalidToken(token)) {
                handleUnauthorized(response, "INVALID_TOKEN", "Access Token이 만료되었거나 유효하지 않습니다.");
                return;
            }

            // 레디스 블랙리스트 검사
            if (redisTemplate.opsForValue().get("BL:" + token) != null) {
                handleUnauthorized(response, "BLACKLISTED_TOKEN", "해당 토큰은 블랙리스트에 등록되어 있습니다.");
                return;
            }

            // 유효한 토큰 → userId 추출 및 SecurityContext 등록
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                userId,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 다음 필터로 진행
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            handleUnauthorized(response, "UNAUTHORIZED", "인증 처리 중 오류가 발생했습니다.");
        }
    }


    /**
     * Authorization 헤더 또는 쿠키에서 토큰 추출
     */
    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("access_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    /**
     * 401 Unauthorized 응답 반환
     */
    private void handleUnauthorized(HttpServletResponse response, String code, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
            String.format("{\"code\":\"%s\",\"message\":\"%s\"}", code, message)
        );
        response.getWriter().flush();
    }
}
