package com.komentum.global.security;

import com.komentum.global.dto.CustomUserDetails;
import com.komentum.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  /**
   * 데이터베이스에서 사용자가 존재하는지 확인 후, CustomUserDetails 반환
   *
   * @param publicUserId 사용자 이메일
   * @return 사용자가 없다면 null, 있다면 CustomUserDetails 반환
   * @throws UsernameNotFoundException
   */
  @Override
  public UserDetails loadUserByUsername(String publicUserId) throws UsernameNotFoundException {
    return userRepository.findByPublicUserId(publicUserId)
        .map(
            res -> CustomUserDetails.builder().userRole(res.getRole()).userEmail(res.getUserEmail()).publicUserId(publicUserId).build())
        .orElse(null);
  }
}
