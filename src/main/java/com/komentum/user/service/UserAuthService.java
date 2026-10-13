package com.komentum.user.service;

import com.komentum.auth.JwtUtils;
import com.komentum.global.dto.CustomUserDetails;
import com.komentum.global.exception.DuplicateResourceException;
import com.komentum.global.exception.UnauthorizedException;
import com.komentum.global.properties.AuthProperty;
import com.komentum.global.security.CustomUserDetailsService;
import com.komentum.global.security.UserRole;
import com.komentum.user.client.KakaoAuthHttpClient;
import com.komentum.user.domain.User;
import com.komentum.user.dto.LocalLoginRequestDto;
import com.komentum.user.dto.PasswordChangeRequsetDto;
import com.komentum.user.dto.SignUpRequestDto;
import com.komentum.user.dto.UserAuthResponse;
import com.komentum.user.repository.UserRepository;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.RequestHeader;

@Slf4j
@Service
public class UserAuthService {

  private final UserRepository userRepository;
  private final TokenService tokenService;
  private final JwtUtils jwtUtils;
  private final KakaoAuthHttpClient kakaoAuthHttpClient;
  private final TransactionTemplate transactionTemplate;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;
  private final CustomUserDetailsService customUserDetailsService;

  public UserAuthService(
      UserRepository userRepository,
      TokenService tokenService,
      KakaoAuthHttpClient kakaoAuthHttpClient,
      TransactionTemplate transactionTemplate,
      JwtUtils jwtUtils, BCryptPasswordEncoder bCryptPasswordEncoder,
      CustomUserDetailsService customUserDetailsService) {
    this.userRepository = userRepository;
    this.tokenService = tokenService;
    this.jwtUtils = jwtUtils;
    this.kakaoAuthHttpClient = kakaoAuthHttpClient;
    this.transactionTemplate = transactionTemplate;
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    this.customUserDetailsService = customUserDetailsService;
  }

  public UserAuthResponse initializeToken(String publicUserId) {
    String accessToken = jwtUtils.generateAccessToken(publicUserId);
    String refreshToken = jwtUtils.generateRefreshToken(publicUserId);
    if (!tokenService.saveAccessAndRefreshToken(publicUserId, accessToken,
        refreshToken)) {
      throw new RuntimeException("failed to save access and refresh token");
    }
    return new UserAuthResponse(accessToken, refreshToken);
  }


  /**
   * <b>로컬 회원가입을 진행한다.</b>
   * <p>1. 가입하려는 이메일의 사용자가 이미 존재하면, 예외를 던진다.</p>
   * <p>2. 인증 정보가 있고 DB에 존재하는 게스트 사용자라면, 기존 사용자를 일반 사용자로 승격한다. (ID 및 리소스 소유권 유지)</p>
   * <p>3. 인증 정보가 없거나 DB에 사용자가 없다면, 새로운 일반 사용자를 생성한다.</p>
   *
   * @param userDetails 현재 요청의 인증 정보 (없으면 null)
   * @throws DuplicateResourceException 이미 가입된 이메일인 경우
   * @throws RuntimeException 인증된 사용자가 GUEST 권한이 아닌 경우
   */
  @Transactional
  public void processLocalSignUp(SignUpRequestDto dto, UserDetails userDetails) {
    // 이미 존재하는 사용자면 예외처리
    if (userRepository.findByUserEmail(dto.getEmail()).isPresent()) {
      throw new DuplicateResourceException("[Local Sign UP] 이미 존재하는 이메일입니다");
    }
    if (userDetails == null) { // 승격할 게스트 정보가 없으면, 신규 일반 사용자 생성
      userRepository.save(dto.toEntity(bCryptPasswordEncoder));
      return;
    }
    userRepository.findByPublicUserId(userDetails.getUsername()).ifPresentOrElse(
        user -> promoteGuestToUser(user, dto),
        () -> userRepository.save(dto.toEntity(bCryptPasswordEncoder))
    );
  }

  /**
   * 기존 게스트 사용자를 일반 사용자로 승격한다. 사용자 ID는 그대로 유지된다.
   *
   * @throws RuntimeException 사용자가 GUEST 권한이 아닌 경우
   */
  private void promoteGuestToUser(User user, SignUpRequestDto dto) {
    if (!user.getRole().equals(UserRole.GUEST)) {
      log.warn("[Local Sign Up] 사용자가 이미 게스트보다 높은 권한으로 등록되어 있음");
      throw new RuntimeException("[Local Sign Up] 사용자가 이미 게스트보다 높은 권한으로 등록되어 있음");
    }
    user.promoteToUser(
        dto.getEmail(),
        bCryptPasswordEncoder.encode(dto.getPassword()),
        "user" + UUID.randomUUID()
    );
  }

  // 로컬 로그인
  public UserAuthResponse processLocalSignIn(LocalLoginRequestDto dto) {
    User user = userRepository.findByUserEmail(dto.getEmail()).orElse(null);
    if (user == null) {
      throw new RuntimeException("This is member information that does not exist.");
    }
    if (user.getUserEmail().equals(dto.getEmail()) &&
        user.matchPassword(dto.getPassword(), bCryptPasswordEncoder)) {
      CustomUserDetails customUserDetails = customUserDetailsService.fromUser(user);
      return initializeToken(customUserDetails.getUsername());
    }
    throw new RuntimeException("incorrect information");
  }

  // 비밀번호 변경
  @Transactional
  public void changePassword(String publicUserId,
      PasswordChangeRequsetDto passwordChangeRequsetDto) {
    User user = userRepository.findByPublicUserId(publicUserId).orElse(null);
    // 기존 비밀번호 검증
    if (user == null) {
      throw new IllegalStateException("유저 정보 오류");
    }
    if (!user.matchPassword(passwordChangeRequsetDto.getCurrentPassword(), bCryptPasswordEncoder)) {
      throw new IllegalStateException("현재 비밀번호가 일치하지 않습니다.");
    }
    user.setEncryptedPassword(
        bCryptPasswordEncoder.encode(passwordChangeRequsetDto.getNewPassword()));
  }

  /**
   * 로그아웃
   */
  @Transactional
  public void handleLogout(@RequestHeader(AuthProperty.ACCESS_TOKEN_HEADER) String accessToken) {
    if (accessToken == null || !jwtUtils.validateToken(accessToken)) {
      throw new RuntimeException("Invalid access token");
    }
    String userEmail = jwtUtils.getUserId(accessToken);
    boolean success1 = tokenService.deleteAccessToken(userEmail);
    boolean success2 = tokenService.deleteRefreshToken(userEmail);
    if (!success1 || !success2) {
      throw new RuntimeException("failed to delete token");
    }
  }

  /**
   * refresh token 으로 토큰 재발급
   */
  public UserAuthResponse doRefreshTokenRotation(String oldRefreshToken) {
    // refresh token 유효성 여부 확인
    if (!jwtUtils.validateToken(oldRefreshToken) || !jwtUtils.isRefreshToken(oldRefreshToken)) {
      throw new UnauthorizedException("[token reissue failed] refresh token is invalid or expired");
    }
    // refresh token rotation ( 기존 refresh 토큰과 비교 및 교체 )
    String publicUserId = jwtUtils.getUserId(oldRefreshToken);
    String newAccessToken = jwtUtils.generateAccessToken(publicUserId);
    String newRefreshToken = jwtUtils.generateRefreshToken(publicUserId);
    boolean rotated = tokenService.rotateRefreshToken(
        publicUserId,
        oldRefreshToken,
        newRefreshToken
    );
    if (!rotated) {
      tokenService.deleteRefreshToken(publicUserId);
      throw new UnauthorizedException("[token reissue failed] refresh token is already reused");
    }
    return new UserAuthResponse(newAccessToken, newRefreshToken);
  }

  /**
   * <b>게스트 토큰을 생성한다.</b>
   * <p>1. 사용자 인증 정보가 없으면, 게스트 사용자를 생성하고 토큰을 발급 한다.</p>
   * <p>2. 사용자 인증 정보가 있고 DB에 없으면, 게스트 사용자를 재생성하고 토큰을 재발급한다.</p>
   * <p>3. 사용자 인증 정보가 있고 DB에 있지만 GUEST 권한이 아니라면, 예외를 던진다.</p>
   * @return 게스트 사용자 토큰
   * */
  @Transactional
  public String createGuestToken(UserDetails userDetails) {
    if (userDetails == null) { // 인증 정보가 없으면, 게스트 사용자 정보 생성 및 게스트 토큰 반환
      return createNewGuestAndReturnToken();
    } else {
      return userRepository.findByPublicUserId(userDetails.getUsername()).map(user -> {
        if (!user.getRole().equals(UserRole.GUEST)) {
          log.warn("[Guest Token] 사용자가 이미 게스트보다 높은 권한으로 등록되어 있음");
          throw new RuntimeException("[Guest Token] 사용자가 이미 게스트보다 높은 권한으로 등록되어 있음");
        }
        return jwtUtils.generateAccessToken(user.getPublicUserId());
      }).orElseGet(this::createNewGuestAndReturnToken);
    }
  }

  /**
   * 게스트 사용자를 생성하고, 게스트 토큰을 발급한다
   * */
  private String createNewGuestAndReturnToken() {
    String publicUserId = UUID.randomUUID().toString();
    User guestUser = User.createGuest(publicUserId);
    userRepository.save(guestUser);
    return jwtUtils.generateAccessToken(publicUserId);
  }
}
