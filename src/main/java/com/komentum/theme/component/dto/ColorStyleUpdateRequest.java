package com.komentum.theme.component.dto;

import com.komentum.theme.component.enums.StyleCode;
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

  @Schema(description = "수정할 color style 이름, null 허용")
  private String name;

  @Schema(description = "수정할 color style 종류, null 허용")
  private StyleCode styleCode;
}