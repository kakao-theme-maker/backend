package com.komentum.global.security;

import com.komentum.global.properties.AuthProperty;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * OAuth2 인증 완료(성공/실패) 후 로그인을 시작한 FE로 되돌아가기 위한 redirect URL을 결정한다.
 * CustomAuthorizationRequestResolver가 검증 후 request attribute에 남긴 값을 사용하고,
 * 값이 없으면 설정된 기본 redirect URL(auth.oauth2-redirect-url)을 사용한다.
 */
@Component
@RequiredArgsConstructor
public class OAuth2RedirectUriResolver {

  private final AuthProperty authProperty;

  /**
   * request에 저장된 FE의 redirect uri를 추출하여 유효한지 확인하고, 유효한 값을 반환한다
   * @return 유효하다면 FE의 redirect uri를, 아니라면 디폴트값을 반환한다
   * */
  public String resolve(HttpServletRequest request) {
    Object feRedirectUri = request.getAttribute(AuthProperty.OAUTH2_FE_REDIRECT_URI_ATTRIBUTE);
    if (feRedirectUri instanceof String uri && !uri.isBlank()) {
      return uri;
    }
    return authProperty.getOauth2RedirectUrl();
  }
}
