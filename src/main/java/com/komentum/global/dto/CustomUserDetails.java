package com.komentum.global.dto;

import com.komentum.global.security.UserRole;
import java.util.Collection;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomUserDetails implements UserDetails {

  @Getter
  private final UserRole userRole;
  private final String userEmail; // 점진적 개선을 위해 남겨두기
  @Getter
  private final String publicUserId;

  @Builder
  public CustomUserDetails(String userEmail, UserRole userRole, String publicUserId) {
    this.userRole = userRole;
    this.userEmail = userEmail;
    this.publicUserId = publicUserId;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(
        new SimpleGrantedAuthority(userRole.getAuthority())
    );
  }

  @Override
  public String getPassword() {
    return null;
  }

  @Override
  public String getUsername() {
    return publicUserId;
  }

}
