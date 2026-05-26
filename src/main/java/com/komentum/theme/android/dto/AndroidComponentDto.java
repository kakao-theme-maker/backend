package com.komentum.theme.android.dto;

import com.komentum.designcomponent.domain.ComponentType;
import com.komentum.designcomponent.domain.DesignComponent;
import com.komentum.theme.core.domain.ThemeImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AndroidComponentDto {

  String imageUrl;
  String AndroidComponentPath;
  String AndroidComponentName;
  Integer sizeX;
  Integer sizeY;

  /**
   * TODO : componentType이 global componentType으로 변경되었으므로 android 전용 componentType을 사용하도록 변경 필요
   * TODO : 현재 PR에서 하기에 너무 무거운 작업이므로 별도 PR에서 관리할 예정
   * */
  @Deprecated
  public static AndroidComponentDto fromEntity(ThemeImage themeImage) {
    DesignComponent component = themeImage.getDesignComponent();
    ComponentType componentType = themeImage.getComponentType();
    return AndroidComponentDto.builder()
        .imageUrl(component.getImageUrl()).build();
  }
}
