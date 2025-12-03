package com.komentum.user.dto;

import com.komentum.user.domain.User;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {

  private String userEmail;
  private String userProfileUrl;
  // 유저 조회에 필요한 정보 추가
  private String userName;
  private LocalDateTime createdAt;
  private int followers;
  private int following;
  private int uploads;



  public static UserResponseDto from(User user, int followers, int following, int uploads) {
    return UserResponseDto.builder()
        .userEmail(user.getUserEmail())
        .userName(user.getName())
        .userProfileUrl(user.getProfileImg())
        .uploads(uploads)
        .followers(followers)
        .following(following)
        .createdAt(user.getCreatedAt())
        .build();
  }

  // 유저 조회 메서드
  //

  // 유저 수정 메서드
  // 이메일 수정 x
  // 비밀번호 수정과 사용자 정보 수정이 구분이 되어 있음
  // 비밀번호 수정할 때 사용자 인증이 필요할 수 있으니,
  // 비밀번호 수정 메서드는 따로 빼야함.
  // 프론트에서 사용자 정보 수정할 때 요청 받아서 null이 아닌 애들만 한번에 수정.

  // 비밀번호 수정 메서드
  // 최소한의 요구 사항만.

}
