package com.komentum.global.security;

import com.komentum.global.dto.CustomOAuth2User;
import com.komentum.global.dto.OAuth2UserInfo;
import com.komentum.user.domain.User;
import com.komentum.user.repository.UserRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomOauth2UserService extends DefaultOAuth2UserService {

  private final UserRepository userRepository;

  @Override
  @Transactional
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    Map<String, Object> attributes = super.loadUser(userRequest).getAttributes();
    String registrationId = userRequest.getClientRegistration().getRegistrationId();
    OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfo.of(registrationId, attributes);
    User serviceUser = createOrRetrieveUser(oAuth2UserInfo);
    return new CustomOAuth2User(serviceUser, attributes);
  }

  @Transactional
  public User createOrRetrieveUser(OAuth2UserInfo oAuth2UserInfo) {
    User serviceUser = userRepository.findByUserEmail(oAuth2UserInfo.getUserEmail()).orElse(null);
    if (serviceUser == null) {
      try {
        serviceUser = userRepository.save(oAuth2UserInfo.toEntity());
      } catch (DataIntegrityViolationException e) {
        // 동시성 문제 대응 : 이미 insert 되었다면 DB의 사용자 사용
        serviceUser = userRepository.findByUserEmail(oAuth2UserInfo.getUserEmail())
            .orElseThrow(() -> e);
      }
    }
    return serviceUser;
  }
}
