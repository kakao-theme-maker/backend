package com.komentum.theme.android.dto;

import com.komentum.theme.component.domain.ComponentType;
import com.komentum.theme.component.domain.DesignComponent;
import com.komentum.theme.theme.domain.ThemeImage;
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
    ComponentType componentType = themeImage.getComponentType();
    return AndroidComponentDto.builder()
        .imageUrl(component.getImageUrl())
        .AndroidComponentName(componentType.getComponentName())
        .AndroidComponentPath(componentType.getComponentPath())
        .sizeX(componentType.getSizeX())
        .sizeY(componentType.getSizeY()).build();
  }
}
