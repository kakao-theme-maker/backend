package com.komentum.user.service;

import com.komentum.auth.AuthProperty;
import com.komentum.auth.JwtUtils;
import com.komentum.user.client.KakaoAuthHttpClient;
import com.komentum.user.domain.User;
import com.komentum.user.dto.LocalLoginRequestDto;
import com.komentum.user.dto.PasswordChangeRequsetDto;
import com.komentum.user.dto.UserAuthResponse;
import com.komentum.user.repository.UserRepository;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
  private final BCryptPasswordEncoder bCryptPasswordEncoder;

  public UserAuthService(
          UserRepository userRepository,
          TokenService tokenService,
          KakaoAuthHttpClient kakaoAuthHttpClient,
          TransactionTemplate transactionTemplate,
          JwtUtils jwtUtils, BCryptPasswordEncoder bCryptPasswordEncoder
  ) {
    this.userRepository = userRepository;
    this.tokenService = tokenService;
    this.jwtUtils = jwtUtils;
    this.kakaoAuthHttpClient = kakaoAuthHttpClient;
    this.transactionTemplate = transactionTemplate;
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
  }
  public UserAuthResponse initializeToken(String email){
    String accessToken = jwtUtils.generateAccessToken(email);
    String refreshToken = jwtUtils.generateRefreshToken(email);
    if (!tokenService.saveAccessAndRefreshToken(email, accessToken,
            refreshToken)) {
      throw new RuntimeException("failed to save access and refresh token");
    }
    return new UserAuthResponse(accessToken, refreshToken);
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
                  return initializeToken(user.getUserEmail());
                }
            )).subscribeOn(Schedulers.boundedElastic())
        );
  }


  // 로컬 회원가입
  public void processLocalSignUp(LocalLoginRequestDto dto){
    User user = userRepository.findById(dto.getEmail()).orElse(null);
    if (user == null) {
      userRepository.save(dto.toEntity(bCryptPasswordEncoder));
    }
  }

  // 로컬 로그인
  public UserAuthResponse processLocalSignIn(LocalLoginRequestDto dto){
    User user = userRepository.findById(dto.getEmail()).orElse(null);
    if (user == null) {
      throw new RuntimeException("This is member information that does not exist.");
    }
    if  (user.getUserEmail().equals(dto.getEmail()) &&
            bCryptPasswordEncoder.matches(dto.getPassword(), user.getEncryptedPassword())) {
      return initializeToken(user.getUserEmail());
    }
    throw new RuntimeException("incorrect information");
  }

  // 비밀번호 변경
  @Transactional
  public void changePassword(String userEmail, PasswordChangeRequsetDto passwordChangeRequsetDto){
    User user = userRepository.findById(userEmail).orElse(null);
    // 기존 비밀번호 검증
    if (user == null){
      throw new IllegalStateException("유저 정보 오류");
    }
    if(!bCryptPasswordEncoder.matches(passwordChangeRequsetDto.getCurrentPassword(), user.getEncryptedPassword())){
      throw new IllegalStateException("현재 비밀번호가 일치하지 않습니다.");
    }
    user.setEncryptedPassword(bCryptPasswordEncoder.encode(passwordChangeRequsetDto.getNewPassword()));
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
