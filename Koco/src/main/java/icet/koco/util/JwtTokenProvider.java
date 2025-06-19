package icet.koco.util;

import icet.koco.user.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    @Value("${JWT_SECRET}")
    private String secret;

    private SecretKey key;
    private final long accessTokenValidity = 1000 * 60 * 30; // 30분
//    private final long accessTokenValidity = 5 * 1000L; // 5초

    private final long refreshTokenValidity = 1000L * 60 * 60 * 24 * 14; // 14일
//    private final long refreshTokenValidity = 5 * 1000L;
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String createAccessToken(User user) {
        return Jwts.builder()
            .setSubject(user.getId().toString())
            .claim("email", user.getEmail())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + accessTokenValidity))
            .signWith(key)
            .compact();
    }

    public String createRefreshToken(User user) {
        return Jwts.builder()
            .setSubject(user.getId().toString())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + refreshTokenValidity))
            .signWith(key)
            .compact();
    }


    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("❌ 만료된 토큰입니다.");
        } catch (SignatureException e) {
            System.out.println("❌ 서명 오류 (key가 맞지 않음).");
        } catch (MalformedJwtException e) {
            System.out.println("❌ 잘못된 형식의 토큰.");
        } catch (Exception e) {
            System.out.println("❌ 기타 오류: " + e.getMessage());
        }
        return false;
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build()
            .parseClaimsJws(token)
            .getBody();
        return Long.valueOf(claims.getSubject());
    }

    // 토큰 만료시간 계산
    public long getExpiration(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getExpiration()
            .getTime() - System.currentTimeMillis();
    }

}