package com.komentum.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사용자 이름 수정 요청 DTO")
public class UserNameUpdateDto {

  @NotBlank(message = "이름은 필수 입력 항목")
  @Schema(description = "새로운 사용자 이름", example = "홍길동", required = true)
  private String name;
}
