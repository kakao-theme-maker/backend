package com.komentum.global.security;

import com.komentum.auth.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtUtils jwtUtils;
  private final CustomUserDetailsService userDetailsService;

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {
    Optional.ofNullable(jwtUtils.resolveJwtToken(request)) // 요청에서 토큰 추출
        .filter(jwtUtils::validateToken) // 추출된 토큰의 유효성 확인
        .map(jwtUtils::getEmail) // 토큰에서 사용자 이메일 추출
        .map(userDetailsService::loadUserByUsername) // 사용자 이메일을 통해 UserDetails 혹은 null을 받음
        .ifPresent( // UserDetails가 null이 아니라면, 인증 정보를 SecurityContext에 저장
            userDetails -> {
              UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                  userDetails, null, userDetails.getAuthorities());
              SecurityContextHolder.getContext().setAuthentication(authToken);
            });
    filterChain.doFilter(request, response);
  }
}
