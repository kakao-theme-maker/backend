package com.komentum.post.dto.query;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO Projection 시 사용할 DTO를 모아두는 집합 클래스
 * DTO Projection으로 한 번에 필요한 DTO 생성이 불가능할 때, 사용할 중간 DTO 모음
 * */
public class ThemeBoardQuery {

  /**
   * ThemeBoardPreviewDto 생성을 위한 중간 계층 DTO
   * */
  @Getter
  @Setter
  @NoArgsConstructor
  public static class Preview {

    private Long postId;
    private Integer themeComponentId;
    private String title;
    private String previewImageName;
    private String userEmail;
    private LocalDateTime createdAt;
    private Long prefers;

    @QueryProjection
    public Preview(Long postId, Integer themeComponentId, String title, String previewImageName,
        String userEmail, LocalDateTime createdAt, Long prefers) {
      this.postId = postId;
      this.themeComponentId = themeComponentId;
      this.title = title;
      this.previewImageName = previewImageName;
      this.userEmail = userEmail;
      this.createdAt = createdAt;
      this.prefers = prefers;
    }
  }
}
