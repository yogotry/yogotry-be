package com.yogotry.domain.auth.controller;

import com.yogotry.domain.auth.dto.LoginRequest;
import com.yogotry.domain.auth.dto.UserInfoResponse;
import com.yogotry.domain.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login/google")
    public ResponseEntity<UserInfoResponse> loginGoogle(
            @RequestBody LoginRequest request,
            HttpServletResponse response) {

        AuthService.LoginResult loginResult = authService.loginWithGoogle(request.getIdToken());

        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", loginResult.accessToken())
                .httpOnly(true)
                .secure(true) // HTTPS 환경에서 true
                .path("/")
                .maxAge(7200) // 2시간
                .sameSite("Strict")
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", loginResult.refreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(60 * 60 * 24) // 1일
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        UserInfoResponse userInfo = new UserInfoResponse(
                loginResult.user().getId(),
                loginResult.user().getEmail(),
                loginResult.user().getNickname()
        );

        return ResponseEntity.ok(userInfo);
    }
}
