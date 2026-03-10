package com.komentum.theme.component.dto;

import com.komentum.theme.component.enums.Platform;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "color style 응답용 DTO")
public class ColorStyleResponse {

  @Schema(description = "color style의 식별자")
  private Integer colorStyleId;

  @Schema(description = "color style의 설명")
  private String explain;

  @Schema(description = "color style의 플랫폼", example = "ANDROID | IOS")
  private Platform platform;

  @Schema(description = "color style의 파일 경로")
  private String styleSheetPath;

  @Schema(description = "color style의 요소 이름")
  private String styleElementName;
  
  @Schema(description = "color style의 속성 이름")
  private String stylePropsName;
}