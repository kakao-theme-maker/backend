package com.komentum.test.dto;

import com.komentum.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TestClientDto {

  private String userEmail;

  public static TestClientDto fromEntity(User client) {
    return TestClientDto.builder()
        .userEmail(client.getUserEmail())
        .build();
  }
}
