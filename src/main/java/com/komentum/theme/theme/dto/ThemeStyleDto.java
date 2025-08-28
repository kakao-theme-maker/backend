package com.komentum.theme.theme.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThemeStyleDto {

  private Integer colorTypeId;
  private String explain;
  private String styleElementName;
  private String stylePropsName;
  private String color;
}
