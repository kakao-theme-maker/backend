package com.komentum.theme.core.dto;

import com.komentum.designcomponent.enums.StyleCode;
import com.komentum.designcomponent.enums.TypeCode;
import com.komentum.theme.core.dto.ThemeUpdateRequest.InsetUpdateDto;
import com.komentum.theme.core.dto.ThemeUpdateRequest.ThemeStyleUpdateRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.Map;
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
@Schema(description = "테마 복제 요청 DTO")
public class ThemeCloneRequest {

  @Schema(description = "복제될 테마의 이름, 미지정 시 임의의 이름이 부여된다")
  private String themeName;

  @NotEmpty
  @Schema(description = "typeCode별 이미지 정보, platformScope=common인 TypeCode를 반드시 포함해야 한다. 사용 가능한 typeCode는 Schemas의 TypeCode 참고")
  private Map<TypeCode, ThemeImageCloneRequest> typeCodes;

  @NotEmpty
  @Schema(description = "styleCode별 색상 정보, ThemeUpdateRequest와 동일한 구조를 사용하며 platformScope=common인 StyleCode를 반드시 포함해야 한다. 사용 가능한 styleCode는 Schemas의 StyleCode 참고")
  private Map<StyleCode, ThemeStyleUpdateRequest> styleCodes;

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "복제할 이미지 정보")
  public static class ThemeImageCloneRequest {

    @Schema(description = "복제할 이미지 식별자(ID)")
    Integer designComponentId;
    @Schema(description = "이미지 inset 정보 ( 말풍선인 경우 ), nullable")
    InsetUpdateDto inset;
  }
}
