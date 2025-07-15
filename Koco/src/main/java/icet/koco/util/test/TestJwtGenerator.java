//package icet.koco.util.test;
//
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.security.Keys;
//import jakarta.annotation.PostConstruct;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Profile;
//import org.springframework.stereotype.Component;
//
//import javax.crypto.SecretKey;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;
//
//
//@Component
//@Profile({"dev", "test"})
//public class TestJwtGenerator implements CommandLineRunner {
//    String secret = "TzzH9y4xTeyKwO9FqWGevKmFkuYs8YXd2q2muydf2fM=";
//
//    private SecretKey key;
//
//    @PostConstruct
//    public void init() {
//        this.key = Keys.hmacShaKeyFor(secret.getBytes());
//    }
//
//    long accessTokenValidity = 1000L * 60 * 60 * 24;            // 1일
//    long refreshTokenValidity = 1000L * 60 * 60 * 24 * 30;      // 30일
//
//    @Override
//    public void run(String... args) {
//        generateTokenCsv();
//    }
//
//    public Map<String, String> createTokenSet(Long userId) {
//        Date now = new Date();
//
//        String accessToken = Jwts.builder()
//                .setSubject(String.valueOf(userId))
//                .setIssuedAt(now)
//                .setExpiration(new Date(now.getTime() + accessTokenValidity))
//                .signWith(key)
//                .compact();
//
//        String refreshToken = Jwts.builder()
//                .setSubject(String.valueOf(userId))
//                .setIssuedAt(now)
//                .setExpiration(new Date(now.getTime() + refreshTokenValidity))
//                .signWith(key)
//                .compact();
//
//        Map<String, String> tokens = new HashMap<>();
//        tokens.put("accessToken", accessToken);
//        tokens.put("refreshToken", refreshToken);
//        return tokens;
//    }
//
//    public void generateTokenCsv() {
//        String fileName = "tokens.csv";
//
//        try (FileWriter writer = new FileWriter(fileName)) {
//            writer.write("user_id,access_token,refresh_token\n");
//
//            for (long userId = 273; userId <= 472; userId++) {
//                Map<String, String> tokenSet = createTokenSet(userId);
//                String line = userId + "," + tokenSet.get("accessToken") + "," + tokenSet.get("refreshToken") + "\n";
//                writer.write(line);
//            }
//
//            System.out.println("✅ CSV 파일이 성공적으로 저장되었습니다: " + fileName);
//        } catch (IOException e) {
//            System.err.println("❌ CSV 파일 저장 중 오류 발생: " + e.getMessage());
//        }
//    }
//
//}
