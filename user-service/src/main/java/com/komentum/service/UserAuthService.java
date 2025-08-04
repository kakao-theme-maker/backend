package com.komentum.service;

import com.komentum.auth.JwtUtils;
import com.komentum.client.KakaoAuthHttpClient;
import com.komentum.constants.AuthProperty;
import com.komentum.domain.User;
import com.komentum.dto.UserAuthResponse;
import com.komentum.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.RequestHeader;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class UserAuthService {

  private final UserRepository userRepository;
  private final TokenService tokenService;
  private final JwtUtils jwtUtils;
  private final KakaoAuthHttpClient kakaoAuthHttpClient;
  private final TransactionTemplate transactionTemplate;

  public UserAuthService(
      UserRepository userRepository,
      TokenService tokenService,
      KakaoAuthHttpClient kakaoAuthHttpClient,
      TransactionTemplate transactionTemplate,
      JwtUtils jwtUtils
  ) {
    this.userRepository = userRepository;
    this.tokenService = tokenService;
    this.jwtUtils = jwtUtils;
    this.kakaoAuthHttpClient = kakaoAuthHttpClient;
    this.transactionTemplate = transactionTemplate;
  }

  /**
   * 카카오 로그인 및 회원가입
   */
  @Transactional
  public Mono<UserAuthResponse> processKakaoAuth(String authCode) {
    return kakaoAuthHttpClient.processLogin(authCode)
        .flatMap(userInfo ->
            Mono.fromCallable(() -> transactionTemplate.execute(status -> {
                  User user = userRepository.findById(userInfo.getEmail()).orElse(null);
                if (user == null) {
                    user = userRepository.save(userInfo.toEntity());
                }
                  String accessToken = jwtUtils.generateAccessToken(user.getUserEmail());
                  String refreshToken = jwtUtils.generateRefreshToken(user.getUserEmail());
                  if (!tokenService.saveAccessAndRefreshToken(user.getUserEmail(), accessToken,
                      refreshToken)) {
                    throw new RuntimeException("failed to save access and refresh token");
                  }
                  return new UserAuthResponse(accessToken, refreshToken);
                }
            )).subscribeOn(Schedulers.boundedElastic())
        );
  }

  /**
   * 로그아웃
   */
  @Transactional
  public void handleLogout(@RequestHeader(AuthProperty.ACCESS_TOKEN_HEADER) String accessToken) {
    if (accessToken == null || !jwtUtils.validateToken(accessToken)) {
      throw new RuntimeException("Invalid access token");
    }
    String userEmail = jwtUtils.getEmail(accessToken);
    boolean success1 = tokenService.deleteAccessToken(userEmail);
    boolean success2 = tokenService.deleteRefreshToken(userEmail);
    if (!success1 || !success2) {
      throw new RuntimeException("failed to delete token");
    }
  }

  /**
   * refresh token 으로 토큰 재발급
   */
  public UserAuthResponse doRefreshTokenRotation(String refreshToken) {
    validateRefreshToken(refreshToken);
    String userEmail = jwtUtils.getEmail(refreshToken);
    String newAccessToken = jwtUtils.generateAccessToken(userEmail);
    String newRefreshToken = jwtUtils.generateRefreshToken(userEmail);
    if (!tokenService.saveAccessAndRefreshToken(userEmail, newAccessToken, newRefreshToken)) {
      throw new RuntimeException("failed to save access and refresh token");
    }
    return new UserAuthResponse(newAccessToken, newRefreshToken);
  }

  /**
   * refresh token 유효성 검사
   */
  void validateRefreshToken(String refreshToken) {
    if (refreshToken == null || !jwtUtils.validateToken(refreshToken)) {
      throw new RuntimeException("Invalid refresh token");
    }
    String email = jwtUtils.getEmail(refreshToken);
    String stored = tokenService.getRefreshToken(email);
    if (stored == null || !stored.equals(refreshToken)) {
        if (stored != null) {
            tokenService.deleteRefreshToken(email);
        }
      throw new RuntimeException("Invalid refresh token : prev version");
    }
  }
}
