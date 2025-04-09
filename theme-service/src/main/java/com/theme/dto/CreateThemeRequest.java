//~ Request : 요청 | 클라이언트 -> 서버
package com.theme.dto;

import jakarta.validation.constraints.NotBlank; //Bean Validation API 유효성 검사 어노테이션
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateThemeRequest {
    @NotNull
    private String userEmail;

    @NotBlank // null 아니면서, 공백 아닌 문자 하나 이상 포함
    private String themeName;

    private String versionName;
    private Boolean isPublic;
    private List<ThemeStyleRequest> styles; // 테마에 포함되는 스타일 목록
    private List<Integer> designComponentIds; // 테마에 포함되는 디자인 컴포넌트 아이디 목록
}
