package com.komentum.theme.theme.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CssCustomizationRequest {
    
    private Integer themeId;
    private String userEmail;
    private List<CssCustomization> customizations;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CssCustomization {
        private Integer colorTypeId;  // ColorStyle의 ID
        private String value;         // 사용자가 입력한 CSS 값 (예: "#FF0000", "'image.png'")
        private String description;   // 설명 (선택사항)
    }
}