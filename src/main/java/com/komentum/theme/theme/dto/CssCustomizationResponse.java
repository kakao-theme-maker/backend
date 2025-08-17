package com.komentum.theme.theme.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CssCustomizationResponse {
    
    private String message;
    private String cssContent;
    private String downloadUrl;
    
    public static CssCustomizationResponse success(String cssContent) {
        return new CssCustomizationResponse("CSS 커스터마이징이 완료되었습니다.", cssContent, null);
    }
    
    public static CssCustomizationResponse successWithDownload(String cssContent, String downloadUrl) {
        return new CssCustomizationResponse("CSS 커스터마이징이 완료되었습니다.", cssContent, downloadUrl);
    }
}