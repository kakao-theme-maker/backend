package com.komentum.global.dto;

import com.komentum.global.security.UserRole;
import java.util.Collection;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class CustomUserDetails implements UserDetails {

  private final UserRole userRole;
  private final String userEmail;

  @Builder
  public CustomUserDetails(String userEmail, UserRole userRole) {
    this.userRole = userRole;
    this.userEmail = userEmail;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(
        new SimpleGrantedAuthority(userRole.name())
    );
  }

  @Override
  public String getPassword() {
    return null;
  }

  @Override
  public String getUsername() {
    return userEmail;
  }
}
