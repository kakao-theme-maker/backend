package com.komentum.global.security;

import com.komentum.global.dto.CustomUserDetails;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentPrincipalProvider {

  /**
   * 현재 사용자의 인증 정보를 반환
   * */
  public Optional<CustomUserDetails> retrievePrincipal() {
    Authentication authentication =
        SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return Optional.empty();
    }
    Object principal = authentication.getPrincipal();
    if (!(principal instanceof CustomUserDetails)) {
      return Optional.empty();
    }
    return Optional.of((CustomUserDetails) principal);
  }
}
