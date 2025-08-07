package com.yogotry.domain.auth.service;

import com.yogotry.domain.user.entity.User;
import com.yogotry.domain.user.repository.UserRepository;
import com.yogotry.domain.user.entity.Oauth;
import com.yogotry.domain.user.repository.OauthRepository;
import com.yogotry.global.auth.google.GoogleIdTokenValidator;
import com.yogotry.global.exception.InvalidAuthorizationCodeException;
import com.yogotry.global.exception.InvalidIdTokenException;
import com.yogotry.global.jwt.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final GoogleIdTokenValidator googleIdTokenValidator;
    private final UserRepository userRepository;
    private final OauthRepository oauthRepository;
    private final JwtUtil jwtUtil;

    public LoginResult loginWithGoogle(String idToken) {
        var payload = googleIdTokenValidator.verify(idToken);
        if (payload == null) {
            throw new InvalidIdTokenException("Invalid ID token.");
        }

        String email = payload.getEmail();
        String providerId = payload.getSubject();

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .name((String) payload.get("name"))
                            .nickname("사용자" + System.currentTimeMillis())
                            .build();
                    return userRepository.save(newUser);
                });

        Oauth oauth = oauthRepository.findByProviderAndProviderId("GOOGLE", providerId)
                .orElseGet(() -> {
                    Oauth newOauth = Oauth.builder()
                            .user(user)
                            .provider("GOOGLE")
                            .providerId(providerId)
                            .build();
                    return oauthRepository.save(newOauth);
                });

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        return new LoginResult(user, accessToken, refreshToken);
    }

    /**
     * 요청 쿠키에서 RefreshToken 추출
     * @param request HttpServletRequest
     * @return refreshToken 문자열
     */
    public String extractRefreshTokenFromRequest(HttpServletRequest request) {
        if (request.getCookies() == null) {
            throw new InvalidAuthorizationCodeException("Refresh token not found.");
        }

        Optional<Cookie> refreshTokenCookie = Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals("refreshToken"))
                .findFirst();

        return refreshTokenCookie.orElseThrow(() -> new InvalidAuthorizationCodeException("Refresh token not found.")).getValue();
    }

    /**
     * refreshToken 검증 후 새로운 AccessToken 발급
     * @param refreshToken 클라이언트에서 전달받은 refreshToken
     * @return 새로 발급된 AccessToken 문자열
     */
    public String refreshAccessToken(String refreshToken) {
        if (!jwtUtil.isValid(refreshToken)) {
            throw new InvalidIdTokenException("Invalid refresh token.");
        }

        // refreshToken에서 사용자 정보 추출 후 AccessToken 재발급
        String userEmail = jwtUtil.getEmailFromToken(refreshToken);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new InvalidIdTokenException("User not found for refresh token."));

        return jwtUtil.generateAccessToken(user);
    }


    public record LoginResult(User user, String accessToken, String refreshToken) {}
}
