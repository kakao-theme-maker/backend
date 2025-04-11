// ~Dto : 응답 | 서버 -> 클라이언트
package com.theme.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThemeComponentDto {
    private Integer themeComponentId;
    private String userEmail;
    private String themeName;
    private String versionNumber;
    private String versionName;
    private Boolean isDone;
    private Boolean isPublic;

    private List<ThemeStyleDto> styles;
    private List<ThemeImageDto> images;
}
