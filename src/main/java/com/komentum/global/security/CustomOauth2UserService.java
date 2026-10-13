package com.komentum.global.security;

import com.komentum.auth.JwtUtils;
import com.komentum.global.dto.CustomOAuth2User;
import com.komentum.global.dto.OAuth2UserInfo;
import com.komentum.global.properties.AuthProperty;
import com.komentum.user.domain.User;
import com.komentum.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
public class CustomOauth2UserService extends DefaultOAuth2UserService {

  private final UserRepository userRepository;
  private final JwtUtils jwtUtils;

  @Override
  @Transactional
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    Map<String, Object> attributes = super.loadUser(userRequest).getAttributes();
    String registrationId = userRequest.getClientRegistration().getRegistrationId();
    OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfo.of(registrationId, attributes);
    User serviceUser = createOrRetrieveUser(oAuth2UserInfo,
        resolveGuestPublicUserId(currentRequest()));
    return new CustomOAuth2User(serviceUser, attributes);
  }

  /**
   * <b>소셜 로그인 사용자 정보로 서비스 사용자를 조회/승격/생성한다.</b>
   * <p>1. 소셜 이메일로 가입된 사용자가 있으면, 해당 사용자를 반환한다. (기존 로그인)</p>
   * <p>2. 게스트 토큰으로 식별한 GUEST 사용자가 DB에 있으면, 해당 사용자를 일반 사용자로 승격한다. (ID 및 리소스 소유권 유지)</p>
   * <p>3. 그 외에는 새로운 사용자를 생성한다. (기존 회원가입)</p>
   *
   * @param guestPublicUserId 요청의 게스트 토큰에서 추출한 사용자 ID (없으면 null)
   */
  @Transactional
  public User createOrRetrieveUser(OAuth2UserInfo oAuth2UserInfo, String guestPublicUserId) {
    // 이미 등록된 사용자라면, 기존 사용자를 조회 및 반환한다.
    User serviceUser = userRepository.findByUserEmail(oAuth2UserInfo.getUserEmail()).orElse(null);
    if (serviceUser != null) {
      return serviceUser;
    }
    // GUEST 토큰인 경우에만 사용자를 USER로 승격시킨다.
    Optional<User> guest = findGuest(guestPublicUserId);
    if (guest.isPresent()) {
      User guestUser = guest.get();
      guestUser.promoteToSocialUser(
          oAuth2UserInfo.getUserEmail(),
          oAuth2UserInfo.getName(),
          oAuth2UserInfo.getProfileImageUrl(),
          oAuth2UserInfo.getAuthProvider()
      );
      return guestUser;
    }
    // 사용자가 DB에도 없고 게스트 토큰도 없다면, 사용자를 생성 및 반환한다
    try {
      serviceUser = userRepository.save(oAuth2UserInfo.toEntity());
    } catch (DataIntegrityViolationException e) {
      // 동시성 문제 대응 : 이미 insert 되었다면 DB의 사용자 사용
      serviceUser = userRepository.findByUserEmail(oAuth2UserInfo.getUserEmail())
          .orElseThrow(() -> e);
    }
    return serviceUser;
  }

  /**
   * 요청의 access token 쿠키/헤더에서 유효한 토큰의 사용자 ID를 추출한다.
   * 카카오 콜백은 JwtAuthFilter보다 먼저 처리되므로, SecurityContext 대신 요청에서 직접 추출한다.
   *
   * @return 유효한 access token이 없으면 null
   */
  public String resolveGuestPublicUserId(HttpServletRequest request) {
    if (request == null) {
      return null;
    }
    return Optional.ofNullable(
            jwtUtils.resolveToken(request, AuthProperty.ACCESS_TOKEN_COOKIE_NAME))
        .filter(jwtUtils::validateToken)
        .filter(jwtUtils::isAccessToken)
        .map(jwtUtils::getUserId)
        .orElse(null);
  }

  /**
   * 사용자 ID로 GUEST 권한의 사용자를 조회한다. GUEST가 아니라면 승격 대상이 아니므로 빈 값을 반환한다.
   */
  private Optional<User> findGuest(String guestPublicUserId) {
    if (guestPublicUserId == null) {
      return Optional.empty();
    }
    return userRepository.findByPublicUserId(guestPublicUserId)
        .filter(user -> user.getRole() == UserRole.GUEST);
  }

  /**
   * 현재 스레드의 HttpServletRequest를 반환한다. 없으면 null
   */
  private HttpServletRequest currentRequest() {
    if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
      return attributes.getRequest();
    }
    return null;
  }
}
