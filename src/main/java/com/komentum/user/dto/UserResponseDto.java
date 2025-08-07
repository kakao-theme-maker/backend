package com.komentum.user.dto;

import com.komentum.user.domain.User;
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

  public static UserResponseDto from(User user) {
    return UserResponseDto.builder()
        .userEmail(user.getUserEmail())
        .userProfileUrl(user.getProfileImg())
        .build();
  }
}
