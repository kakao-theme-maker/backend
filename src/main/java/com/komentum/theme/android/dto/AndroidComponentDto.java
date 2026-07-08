package com.komentum.theme.android.dto;

import com.komentum.designcomponent.domain.PlatformComponentType;
import com.komentum.global.enums.FileExtension;
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
  Integer sizeX;
  Integer sizeY;
  FileExtension fileExtension;

  public static AndroidComponentDto fromEntity(PlatformComponentType platformComponentType,
      String imageUrl) {
    return AndroidComponentDto.builder()
        .imageUrl(imageUrl)
        .imageFilePath(platformComponentType.getPath())
        .sizeX(platformComponentType.getWidth())
        .sizeY(platformComponentType.getHeight())
        .fileExtension(platformComponentType.getFileExtension())
        .build();
  }
}
