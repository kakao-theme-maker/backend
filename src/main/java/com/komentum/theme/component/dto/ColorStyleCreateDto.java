package com.komentum.theme.component.dto;

import com.komentum.theme.component.enums.Platform;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "color style 생성 요청용 DTO")
public class ColorStyleCreateDto {

  @NotBlank(message = "설명은 필수입니다")
  @Schema(description = "생성할 color style의 설명")
  private String explain;

  @NotNull(message = "플랫폼은 필수입니다")
  @Schema(description = "생성할 color style의 플랫폼", example = "ANDROID | IOS")
  private Platform platform;

  @NotBlank(message = "스타일시트 경로는 필수입니다")
  @Schema(description = "생성할 color style의 경로")
  private String styleSheetPath;

  @NotBlank(message = "스타일 요소명은 필수입니다")
  @Schema(description = "생성할 color style의 요소 이름")
  private String styleElementName;

  @NotBlank(message = "스타일 속성명은 필수입니다")
  @Schema(description = "생성할 color style의 속성 이름")
  private String stylePropsName;
}