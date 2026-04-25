package com.komentum.global.security;

import com.komentum.auth.JwtUtils;
import com.komentum.global.properties.AuthProperty;
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
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtUtils jwtUtils;
  private final CustomUserDetailsService userDetailsService;

  private final RequestMatcher reissueMatcher =
      new AntPathRequestMatcher("/api/auth/reissue", "POST");

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {
    // access token 기반 인증 및 인가
    handleAccessToken(request);
    // 토큰 재발급 요청인 경우, 유효한 refresh token가 있어야하고, 없으면 401 응답을 반환한다
    if (isReissueRequest(request) && !handleRefreshToken(request)) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }
    filterChain.doFilter(request, response);
  }

  /**
   * access token을 추출하고, 유효한다면 Authentication 객체를 추가(인가)한다.
   * */
  private void handleAccessToken(HttpServletRequest request) {
    Optional.ofNullable(
            jwtUtils.resolveToken(request, AuthProperty.ACCESS_TOKEN_COOKIE_NAME)) // 요청에서 토큰 추출
        .filter(jwtUtils::validateToken) // 추출된 토큰의 유효성 확인
        .filter(jwtUtils::isAccessToken) // 토큰이 access token인지 확인
        .map(jwtUtils::getUserId) // 토큰에서 사용자 아이디 추출
        .map(userDetailsService::loadUserByUsername) // 사용자 아이디를 통해 UserDetails 혹은 null을 받음
        .ifPresent( // UserDetails가 null이 아니라면, 인증 정보를 SecurityContext에 저장
            userDetails -> {
              UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                  userDetails, null, userDetails.getAuthorities());
              SecurityContextHolder.getContext().setAuthentication(authToken);
            });
  }

  /**
   * 현재 요청이 토큰 재발급 요청인지 확인한다
   * */
  private boolean isReissueRequest(HttpServletRequest request) {
    return reissueMatcher.matches(request);
  }

  /**
   * 현재 요청에 유효한 REFRESH_TOKEN이 존재하는지 확인한다.
   * */
  private boolean handleRefreshToken(HttpServletRequest request) {
    String refreshToken = jwtUtils.resolveToken(request, AuthProperty.REFRESH_TOKEN_COOKIE_NAME);
    return refreshToken != null &&
        jwtUtils.validateToken(refreshToken) &&
        jwtUtils.isRefreshToken(refreshToken);
  }
}
