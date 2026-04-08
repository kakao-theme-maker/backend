package com.komentum.theme.theme.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "테마 이미지 요청용 DTO")
public class ThemeImageRequest {

  @Schema(description = "디자인 컴포넌트의 식별자", example = "1")
  Integer designComponentId;

  @Schema(description = "컴포넌트 타입의 식별자", example = "1")
  Integer componentTypeId;
}
