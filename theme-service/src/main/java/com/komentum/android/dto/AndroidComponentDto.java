package com.komentum.android.dto;

import com.komentum.component.domain.ComponentType;
import com.komentum.component.domain.DesignComponent;
import com.komentum.theme.domain.ThemeImage;
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

  public static AndroidComponentDto fromEntity(ThemeImage themeImage) {
    DesignComponent component = themeImage.getDesignComponent();
    ComponentType componentType = component.getComponentType();
    return AndroidComponentDto.builder()
        .imageUrl(component.getImageUrl())
        .AndroidComponentName(componentType.getAndroidComponentName())
        .AndroidComponentPath(componentType.getAndroidComponentPath())
        .sizeX(componentType.getSizeX())
        .sizeY(componentType.getSizeY()).build();
  }
}
