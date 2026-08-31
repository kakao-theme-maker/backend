package com.komentum.designcomponent.dto;

import com.komentum.designcomponent.enums.PlatformScope;
import com.komentum.designcomponent.enums.StyleCode;
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

  @NotNull
  @Schema(description = "color style의 이름")
  private String name;

  @NotNull
  @Schema(description = "color style의 종류")
  private StyleCode styleCode;

  @NotNull(message = "플랫폼 구분은 필수입니다")
  @Schema(description = "생성할 color style이 공통/Android/iOS 중 어디에 속하는지, not null")
  private PlatformScope platformScope;
}