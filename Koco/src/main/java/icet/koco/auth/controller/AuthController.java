package icet.koco.auth.controller;

import icet.koco.auth.dto.*;
import icet.koco.auth.service.AuthService;
import icet.koco.auth.service.LogoutService;
import icet.koco.global.exception.UnauthorizedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/backend/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "카카오 OAuth 인증 관련 API")
public class AuthController {

    private final AuthService authService;
    private final LogoutService logoutService;

    @GetMapping("/callback")
    @Operation(summary = "카카오 콜백")
    public ResponseEntity<AuthResponse> kakaoCallback(@RequestParam("code") String code,
                                                      HttpServletResponse response) {
        System.out.println("인가코드: " + code);
        AuthResponse authResponse = authService.loginWithKakao(code, response);
        return ResponseEntity.ok(authResponse);
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(HttpServletRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(logoutService.logout(request, response));
    }

    @Operation(summary = "리프레쉬 토큰 기반 재발급")
    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refreshToken(
            @CookieValue(value = "refresh_token", required = true) String refreshToken,
            HttpServletResponse response) {
        try {
            System.out.println("refresh_token: " + refreshToken);
            RefreshResponse refreshResponse = authService.refreshAccessToken(refreshToken, response);
            return ResponseEntity.ok(refreshResponse);
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}