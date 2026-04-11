package com.komentum.theme.theme.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "테마 이미지 응답용 DTO")
public class ThemeImageDto {

  @Schema(description = "디자인 컴포넌트의 식별자", example = "1")
  private Integer designComponentId;
}
