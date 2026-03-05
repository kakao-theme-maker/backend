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
@Schema(description = "사용자 비밀번호 수정 요청 DTO")
public class PasswordChangeRequsetDto {

  @Schema(description = "현재 사용자 비밀번호(수정 전)", example = "test1234")
  private String currentPassword;
  @Schema(description = "새로운 비밀번호(수정 후)", example = "new_password")
  private String newPassword;

}
