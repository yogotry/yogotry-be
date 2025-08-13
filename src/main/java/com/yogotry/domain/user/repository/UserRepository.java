package com.yogotry.domain.user.repository;

import com.yogotry.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * 기능: 이메일을 기준으로 하나의 User 엔티티 조회
     * 결과가 있으면 Optional 안에 User가 들어 있고, 없으면 Optional.empty()가 반환됨
     * 예시: 로그인 시 해당 이메일로 가입된 사용자가 있는 확인할 때 사용
     */
    Optional<User> findByEmail(String email);
}
