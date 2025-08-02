package com.yogotry.domain.user.repository;

import com.yogotry.domain.user.entity.Oauth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OauthRepository extends JpaRepository<Oauth, Long> {
    /**
     * 기능: provider와 providerId 조합으로 하나의 Oauth 엔티티 조회
     * provider -> google
     * providerId -> 해당 OAuth 제공자가 발급한 고유 유저 식별자
     * OAuth 로그인 시 이 조합으로 이미 등록된 계정인지 확인할 때 사용
     */
    Optional<Oauth> findByProviderAndProviderId(String provider, String providerId);
}
