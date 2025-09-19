package com.komentum.theme.component.dto;

import com.komentum.theme.component.enums.Platform;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateColorStyleRequest {

  private String explain;
  private Platform platform;
  private String styleSheetPath;
  private String styleElementName;
  private String stylePropsName;
}