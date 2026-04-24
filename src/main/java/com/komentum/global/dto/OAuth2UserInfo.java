package com.komentum.global.dto;

import com.komentum.global.security.UserRole;
import com.komentum.user.domain.AuthProvider;
import com.komentum.user.domain.User;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2UserInfo {

  private String userEmail;
  private String profileImageUrl;
  private String name;
  private AuthProvider authProvider;

  public User toEntity() {
    return User.builder()
        .userEmail(this.userEmail)
        .profileImgUrl(profileImageUrl)
        .authProvider(authProvider)
        .name(name)
        .role(UserRole.USER)
        .publicUserId(UUID.randomUUID().toString())
        .build();
  }

  public static OAuth2UserInfo of(String registrationId, Map<String, Object> attributes) {
    AuthProvider provider = AuthProvider.from(registrationId);
    return switch (provider) {
      case KAKAO -> ofKakao(attributes, provider);
      case LOCAL -> throw new IllegalArgumentException("invalid oauth2 provider");
    };
  }

  private static OAuth2UserInfo ofKakao(Map<String, Object> attributes, AuthProvider authProvider) {
    Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
    Map<String, Object> profile = (Map<String, Object>) account.get("profile");
    return OAuth2UserInfo.builder()
        .name((String) profile.get("nickname"))
        .userEmail((String) account.get("email"))
        .profileImageUrl((String) profile.get("profile_image_url"))
        .authProvider(authProvider)
        .build();
  }
}
