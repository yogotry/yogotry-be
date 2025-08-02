package com.yogotry.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  // 이게 요즘 권장되는 람다 스타일
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login/google").permitAll()
                        .anyRequest().authenticated()
                );
        return http.build();
    }
}
