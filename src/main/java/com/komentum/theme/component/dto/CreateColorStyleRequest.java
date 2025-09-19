package com.komentum.theme.component.dto;

import com.komentum.theme.component.enums.Platform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateColorStyleRequest {

  @NotBlank(message = "설명은 필수입니다")
  private String explain;

  @NotNull(message = "플랫폼은 필수입니다")
  private Platform platform;

  @NotBlank(message = "스타일시트 경로는 필수입니다")
  private String styleSheetPath;

  @NotBlank(message = "스타일 요소명은 필수입니다")
  private String styleElementName;

  @NotBlank(message = "스타일 속성명은 필수입니다")
  private String stylePropsName;
}