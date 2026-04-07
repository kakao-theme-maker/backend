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
@Schema(description = "Color Style 갱신을 위한 요청 DTO")
public class ColorStyleUpdateRequest {

  @Schema(description = "수정할 color style 설명, null 허용")
  private String explain;

  @Schema(description = "수정할 color style 플랫폼, null 허용", example = "ANDROID | IOS")
  private Platform platform;

  @Schema(description = "수정할 color style 파일 경로, null 허용")
  private String styleSheetPath;

  @Schema(description = "수정할 color style 요소 이름, null 허용")
  private String styleElementName;

  @Schema(description = "수정할 color style 속성 이름, null 허용")
  private String stylePropsName;
}