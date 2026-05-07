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
@Schema(description = "color style 응답용 DTO")
public class ColorStyleResponse {

  @Schema(description = "color style의 식별자")
  private Integer colorStyleId;

  @Schema(description = "color style의 설명")
  private String explain;

  @Schema(description = "color style 이름")
  private String name;

  // API 응답 시 enum name 대신 styleCode string 사용 ( @JsonValue )
  @Schema(description = "color style 종류")
  private StyleCode styleCode;
}