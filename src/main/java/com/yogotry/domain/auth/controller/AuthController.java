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
            @RequestBody(required = false) Map<String, String> body,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String bodyCode,
            HttpServletResponse response) {

        String authCode = null;
        
        // JSON body에서 code 추출 시도
        if (body != null && body.get("code") != null) {
            authCode = body.get("code");
        }
        // Query parameter에서 code 추출 시도
        else if (code != null) {
            authCode = code;
        }
        // bodyCode parameter에서 추출 시도
        else if (bodyCode != null) {
            authCode = bodyCode;
        }
        
        if (authCode == null || authCode.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // 1) code로 id_token 얻기
        String idToken = googleOAuthService.exchangeCodeForIdToken(authCode);

        // 2) id_token으로 사용자 로그인 처리, JWT 발급
        AuthService.LoginResult loginResult = authService.loginWithGoogle(idToken);

        // 3) JWT 쿠키 생성 및 응답 헤더에 추가
        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", loginResult.accessToken())
                .httpOnly(true)
                .secure(false) // 개발 환경에서는 false
                .path("/")
                .maxAge(7200)
                .sameSite("Lax") // 개발 환경에서는 Lax
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", loginResult.refreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(60 * 60 * 24)
                .sameSite("Lax")
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