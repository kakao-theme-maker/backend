package com.komentum.theme.core.dto;

import com.komentum.designcomponent.domain.ComponentType;
import com.komentum.designcomponent.domain.DesignComponent;
import com.komentum.designcomponent.enums.TypeCode;
import com.komentum.designcomponent.enums.TypeCodeGroup;
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
@Schema(description = "테마 내 이미지 정보")
public class ThemeDesignAssetDto {

  @Schema(description = "이미지 종류")
  TypeCode typeCode;
  @Schema(description = "이미지 종류 대분류")
  TypeCodeGroup typeCodeGroup;
  @Schema(description = "이미지 종류 대분류 이름")
  String typeCodeGroupName;
  @Schema(description = "디자인 에셋 식별자")
  Integer designComponentId;
  @Schema(description = "디자인 에셋 URL")
  String imageUrl;

  /**
   * Entity로부터 ThemeDesignAssetDto를 생성한다
   * */
  public static ThemeDesignAssetDto from(ComponentType componentType,
      DesignComponent designComponent) {
    return ThemeDesignAssetDto.builder()
        .designComponentId(designComponent.getDesignComponentId())
        .typeCodeGroup(componentType.getTypeCode().getTypeCodeGroup())
        .typeCodeGroupName(componentType.getTypeCode().getTypeCodeGroup().getDescription())
        .imageUrl(designComponent.getImageUrl())
        .typeCode(componentType.getTypeCode())
        .build();
  }
}
