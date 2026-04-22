// ~Dto : 응답 | 서버 -> 클라이언트
package com.komentum.theme.theme.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "테마 컴포넌트 응답용 DTO")
public class ThemeComponentDto {

  @Schema(description = "테마 컴포넌트의 식별자", example = "1")
  private Integer themeComponentId;

  @Schema(description = "테마를 생성한 사용자의 이메일", example = "user@example.com")
  private String userEmail;

  @Schema(description = "테마 이름", example = "My Custom Theme")
  private String themeName;

  @Schema(description = "테마 버전 번호", example = "1.0.0")
  private String versionNumber;

  @Schema(description = "테마 버전 이름", example = "Initial Release")
  private String versionName;

  @Schema(description = "테마 완성 여부", example = "true")
  private Boolean isDone;

  @Schema(description = "테마 공개 여부", example = "false")
  private Boolean isPublic;

  @Schema(description = "테마 생성일시", example = "2026-03-08T21:21:08")
  private LocalDateTime createdAt;

  @Schema(description = "테마 대표 이미지로 사용되는 design component 이미지 URL", example = "https://example.com/design-components/profile-image.png")
  private String previewImageUrl;

  @Schema(description = "테마에 포함된 스타일 목록")
  private List<ThemeStyleDto> styles;

  @Schema(description = "테마에 포함된 이미지 목록")
  private List<ThemeImageDto> images;
}
