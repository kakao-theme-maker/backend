package com.komentum.global.security;

import com.komentum.global.dto.CustomUserDetails;
import com.komentum.user.domain.User;
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

  /**
  * User 엔티티를 CustomUserDetails로 변환
   *
   * 서비스 레이어에서 User 필드에 직접 접근하지 않고, CustomUSerDetails를 통해서 접근하기 위한 메서드
   *
   * @param user 변환할 User 엔티티
   * @return user가 null 이라면 null, 아니면 CustomUSerDetails
  * */
  public CustomUserDetails fromUser(User user){
    if(user == null){
      return null;
    }
    return CustomUserDetails.builder()
        .userRole(user.getRole())
        .userEmail(user.getUserEmail())
        .publicUserId(user.getPublicUserId())
        .build();
  }
}
