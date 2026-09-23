package com.komentum.theme.core.dto;

import com.komentum.designcomponent.domain.ComponentType;
import com.komentum.designcomponent.domain.DesignComponent;
import com.komentum.designcomponent.enums.PlatformScope;
import com.komentum.designcomponent.enums.StyleCode;
import com.komentum.designcomponent.enums.TypeCode;
import com.komentum.designcomponent.enums.TypeCodeGroup;
import com.komentum.theme.core.domain.ImageInset;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "테마 상세 조회 응답 DTO")
public class ThemeDetailResponse {

  @Schema(description = "테마 식별자")
  private Integer themeComponentId;
  @Schema(description = "테마 이름")
  private String themeName;
  @Schema(description = "typeCode별 이미지 정보")
  Map<TypeCode, TypeCodeInfo> typeCodes;
  @Schema(description = "styleCode별 색상 정보")
  Map<StyleCode, StyleCodeInfo> styleCodes;

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "typeCode에 대한 색상 정보")
  public static class TypeCodeInfo {

    @Schema(description = "디자인 에셋 ID")
    Integer designComponentId;
    @Schema(description = "디자인 에셋 이미지 URL")
    String imageUrl;
    @Schema(description = "호환되는 플랫폼 정보", example = "ANDROID | COMMON | IOS")
    PlatformScope platformScope;
    @Schema(description = "이미지 대분류")
    TypeCodeGroup typeCodeGroup;
    @Schema(description = "이미지 대분류 이름")
    String typeCodeGroupName;
    @Schema(description = "이미지 인셋 정보 ( 인셋이 없으면 NULL )")
    InsetResponseDto inset;

    /**
     * @param imageUrl FileManager를 통해 designComponent의 fileName으로부터 생성한 이미지 URL
     */
    public static TypeCodeInfo of(DesignComponent designComponent, ComponentType componentType,
        ImageInset inset, String imageUrl) {
      TypeCode typeCode = componentType.getTypeCode();
      return TypeCodeInfo.builder()
          .designComponentId(designComponent.getDesignComponentId())
          .imageUrl(imageUrl)
          .platformScope(componentType.getPlatformScope())
          .typeCodeGroup(typeCode.getTypeCodeGroup())
          .typeCodeGroupName(typeCode.getTypeCodeGroup().getDescription())
          .inset(InsetResponseDto.from(inset))
          .build();
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    static class InsetResponseDto {

      Integer stretchX;
      Integer stretchY;
      Integer top;
      Integer bottom;
      Integer left;
      Integer right;

      public static InsetResponseDto from(ImageInset inset) {
        if (inset == null) {
          return null;
        }
        return InsetResponseDto.builder()
            .stretchX(inset.getStretchX())
            .stretchY(inset.getStretchY())
            .top(inset.getEdgeInsetTop())
            .bottom(inset.getEdgeInsetBottom())
            .left(inset.getEdgeInsetLeft())
            .right(inset.getEdgeInsetRight())
            .build();
      }
    }
  }

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class StyleCodeInfo {

    String color;
    Integer alpha;
    PlatformScope platformScope;

    public static StyleCodeInfo of(String color, Integer alpha, PlatformScope platformScope) {
      return StyleCodeInfo.builder()
          .color(color)
          .alpha(alpha)
          .platformScope(platformScope)
          .build();
    }
  }
}
