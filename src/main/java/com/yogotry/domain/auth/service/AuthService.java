package com.yogotry.domain.auth.service;

import com.yogotry.domain.auth.dto.LoginRequest;
import com.yogotry.domain.user.entity.User;
import com.yogotry.domain.user.repository.UserRepository;
import com.yogotry.domain.user.entity.Oauth;
import com.yogotry.domain.user.repository.OauthRepository;
import com.yogotry.global.auth.google.GoogleIdTokenValidator;
import com.yogotry.global.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
            throw new IllegalArgumentException("Invalid ID token.");
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

    public record LoginResult(User user, String accessToken, String refreshToken) {}
}
