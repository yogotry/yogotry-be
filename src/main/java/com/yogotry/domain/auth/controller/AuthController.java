package com.yogotry.domain.auth.controller;

import com.yogotry.domain.auth.dto.LoginRequest;
import com.yogotry.domain.auth.dto.UserInfoResponse;
import com.yogotry.domain.auth.service.AuthService;
import com.yogotry.domain.auth.service.GoogleOAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final GoogleOAuthService googleOAuthService;
    private final AuthService authService;

    @PostMapping("/login/google")
    public ResponseEntity<UserInfoResponse> loginGoogle(
            @RequestBody Map<String, String> body,
            HttpServletResponse response) {

        String code = body.get("code");
        if (code == null || code.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // 1) code로 id_token 얻기
        String idToken = googleOAuthService.exchangeCodeForIdToken(code);

        // 2) id_token으로 사용자 로그인 처리, JWT 발급
        AuthService.LoginResult loginResult = authService.loginWithGoogle(idToken);

        // 3) JWT 쿠키 생성 및 응답 헤더에 추가
        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", loginResult.accessToken())
                .httpOnly(true)
                .secure(true) // HTTPS 환경에서만 true
                .path("/")
                .maxAge(7200)
                .sameSite("Strict")
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", loginResult.refreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(60 * 60 * 24)
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        // 4) 사용자 정보 응답 바디에 담아서 반환
        UserInfoResponse userInfo = new UserInfoResponse(
                loginResult.user().getId(),
                loginResult.user().getEmail(),
                loginResult.user().getNickname()
        );

        return ResponseEntity.ok(userInfo);
    }
}