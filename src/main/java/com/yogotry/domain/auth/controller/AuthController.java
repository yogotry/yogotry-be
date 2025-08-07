package com.yogotry.domain.auth.controller;

import com.yogotry.domain.auth.dto.LoginRequest;
import com.yogotry.domain.auth.dto.UserInfoResponse;
import com.yogotry.domain.auth.service.AuthService;
import com.yogotry.domain.auth.service.GoogleOAuthService;
import com.yogotry.domain.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final GoogleOAuthService googleOAuthService;
    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/login/google")
    public ResponseEntity<?> loginGoogle(
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
            throw new IllegalArgumentException("Invalid Authorization Code");
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
                loginResult.user().getNickname(),
                loginResult.user().getLastLoginAt()
        );

        // 최초 로그인 여부 판단 (예시: 마지막 접속시간이 없으면 최초 로그인으로 간주)
        boolean isFirstLogin = loginResult.user().getLastLoginAt() == null;

        // 마지막 로그인 시간 업데이트
        loginResult.user().setLastLoginAt(ZonedDateTime.now());
        userRepository.save(loginResult.user());

        if (isFirstLogin) {
            // 최초 로그인(회원가입) 시 201 Created로 응답
            return ResponseEntity.status(201).body(Map.of(
                "code", 201,
                "message", "First Login - User Registered",
                "data", userInfo
            ));
        } else {
            // 일반 로그인 시 200 OK로 응답
            return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "Login Successful",
                "data", userInfo
            ));
        }
    }

    /**
     * 리프레시 토큰으로 새로운 AccessToken 발급 API
     * - 요청 쿠키에서 refreshToken 추출 후 유효성 검사 및 AccessToken 재발급
     * - 새로운 AccessToken 쿠키를 응답에 포함
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = authService.extractRefreshTokenFromRequest(request);

        // refreshToken 검증 및 새로운 accessToken 발급
        String newAccessToken = authService.refreshAccessToken(refreshToken);

        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", newAccessToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(7200)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());

        // 200 OK, message 포함
        return ResponseEntity.ok(Map.of(
            "code", 200,
            "message", "Access token refreshed"
        ));
    }

    /**
     * 로그아웃 API
     * - 클라이언트에 저장된 AccessToken, RefreshToken 쿠키를 삭제(만료시간 0으로 설정)
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // 만료된 쿠키 생성 (쿠키 삭제)
        ResponseCookie deleteAccessTokenCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        ResponseCookie deleteRefreshTokenCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, deleteAccessTokenCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, deleteRefreshTokenCookie.toString());

        // 200 OK, message 포함
        return ResponseEntity.ok(Map.of(
            "code", 200,
            "message", "Logout successful"
        ));
    }
}