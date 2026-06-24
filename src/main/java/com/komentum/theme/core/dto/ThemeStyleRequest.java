package com.komentum.theme.core.dto;

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
@Schema(description = "테마 스타일 요청용 DTO")
public class ThemeStyleRequest {

  @NotNull
  @Schema(description = "컬러 스타일의 식별자", example = "1")
  private Integer colorStyleId;

  @Schema(description = "컬러 값", example = "#FF5733")
  private String color;
}
