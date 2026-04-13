package com.komentum.user.dto;

import com.komentum.user.domain.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사용자 성별 수정 요청 DTO")
public class UserGenderUpdateDto {

  @NotNull(message = "성별은 필수 입력 항목")
  @Schema(description = "사용자 성별", example = "male", required = true)
  private Gender gender;
}
