package com.theme.dto;

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
    private String iosStyleName;
    private String androidStyleName;
    private String color;
}
