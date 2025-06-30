package icet.koco.config;

import icet.koco.util.JwtAuthenticationFilter;
import icet.koco.util.JwtTokenProvider;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
        JwtTokenProvider jwtTokenProvider,
        RedisTemplate<String, String> redisTemplate) throws Exception {

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))  // CORS 설정 추가
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/backend/v1/auth/**","/api/backend/v1/solution", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**", "/actuator/**", "/api/backend/admin/today/problem-set", "/api/backend/test/timezone")
                .permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, redisTemplate), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
            "http://172.16.24.28:8000",         // FastAPI 서버 주소
            "http://localhost:5173",            // 프론트 로컬 주소
            "https://ktbkoco.com",              // 운영서버 도메인 주소
            "https://koco-admin.o-r.kr",         // 개발서버 도메인 주소
            "https://koco.click"         // 개발서버 도메인 주소
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true); // 쿠키 허용
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

