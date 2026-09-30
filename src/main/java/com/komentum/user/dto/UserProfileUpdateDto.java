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

  @Pattern(regexp = "(?s).*\\P{javaWhitespace}.*", message = "이름은 필수 입력 항목")
  @Schema(description = "새로운 사용자 이름", example = "홍길동")
  private String name;

  @Size(max = 100, message = "한줄소개는 100자 이하여야 합니다")
  @Schema(description = "한줄소개 (줄바꿈 허용)", example = "안녕하세요.\n반갑습니다.")
  private String introduce;
}
