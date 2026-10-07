package com.komentum.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사용자 프로필 수정 요청 DTO")
public class UserProfileUpdateDto {

  @Pattern(regexp = "(?s).*\\P{javaWhitespace}.*", message = "이름은 빈 문자열이나 공백만 입력할 수 없습니다")
  @Schema(description = "선택 입력. 생략하거나 null이면 기존 이름 유지, 빈 문자열·공백만 있는 값 불가",
      example = "홍길동", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String name;

  @Size(max = 100, message = "한줄소개는 100자 이하여야 합니다")
  @Schema(description = "선택 입력. 생략하거나 null이면 기존 소개 유지, 빈 문자열이면 소개 비움. 공백만 있는 값도 허용, 줄바꿈 포함 최대 100자",
      example = "안녕하세요.\n반갑습니다.", maxLength = 100, nullable = true,
      requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String introduce;
}
