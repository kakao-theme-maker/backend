package com.komentum.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사용자 생년월일 수정 요청 DTO")
public class UserBirthUpdateDto {

  @NotNull(message = "생년월일은 필수 입력 항목")
  @Schema(description = "사용자 생년월일", example = "2000-01-01", required = true)
  private LocalDate birth;
}
