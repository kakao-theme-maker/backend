package com.komentum.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "로컬 로그인 요청 DTO")
public class LocalLoginRequestDto {

  @Schema(description = "사용자 이메일", example = "test@test.com")
  private String email;
  @Schema(description = "사용자 비밀번호", example = "test1234")
  private String password;

}
