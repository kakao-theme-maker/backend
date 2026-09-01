package com.komentum.theme.android.dto;

import com.komentum.designcomponent.domain.DesignComponent;
import com.komentum.designcomponent.domain.PlatformComponentType;
import com.komentum.global.enums.FileExtension;
import com.komentum.theme.core.domain.ImageInset;
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
  String imageFilePath;
  ImageInset imageInset;
  Integer sizeX;
  Integer sizeY;
  FileExtension fileExtension;

  public static AndroidComponentDto fromEntity(PlatformComponentType platformComponentType,
      DesignComponent designComponent, ImageInset imageInset) {
    return AndroidComponentDto.builder()
        .imageUrl(designComponent.getImageUrl())
        .imageFilePath(platformComponentType.getPath())
        .imageInset(imageInset)
        .sizeX(platformComponentType.getWidth())
        .sizeY(platformComponentType.getHeight())
        .fileExtension(platformComponentType.getFileExtension())
        .build();
  }
}
