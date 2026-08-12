//~ Request : 요청 | 클라이언트 -> 서버
package com.komentum.theme.core.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "테마 생성/수정 요청용 DTO")
public class CreateThemeRequest {

  @NotNull
  @Schema(description = "테마를 생성하는 사용자의 이메일 (필수)", example = "user@example.com")
  private String userEmail;

  @NotBlank // null 아니면서, 공백 아닌 문자 하나 이상 포함
  @Schema(description = "테마 이름", example = "My Custom Theme")
  private String themeName;

  @Schema(description = "테마 버전 이름", example = "Initial Release")
  private String versionName;

  @Schema(description = "테마 공개 여부", example = "false")
  private Boolean isPublic;

  @Schema(description = "테마에 포함되는 스타일 목록")
  private List<ThemeStyleRequest> styles; // 테마에 포함되는 스타일 목록

  @Schema(description = "테마에 포함되는 이미지 목록")
  private List<ThemeImageRequest> images; // 테마에 포함되는 디자인 컴포넌트 아이디 목록
}
