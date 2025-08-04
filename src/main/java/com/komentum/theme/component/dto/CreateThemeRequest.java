//~ Request : 요청 | 클라이언트 -> 서버
package com.komentum.theme.component.dto;

import com.komentum.theme.theme.dto.ThemeStyleRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
