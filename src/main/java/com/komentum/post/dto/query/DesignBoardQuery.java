package com.komentum.post.dto.query;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import lombok.Getter;

public class DesignBoardQuery {

  @Getter
  public static class Detail {

    private final Long postId;
    private final String title;
    private final String content;
    private final Integer designComponentId;
    private final String userEmail;
    private final LocalDateTime createdAt;
    private final String previewImageName;
    private final Long prefers;

    @QueryProjection
    public Detail(Long postId, String title, String content, Integer designComponentId,
        String userEmail, LocalDateTime createdAt, String previewImageName, Long prefers) {
      this.postId = postId;
      this.title = title;
      this.content = content;
      this.designComponentId = designComponentId;
      this.userEmail = userEmail;
      this.createdAt = createdAt;
      this.previewImageName = previewImageName;
      this.prefers = prefers;
    }
  }

  @Getter
  public static class Preview {

    private final Long postId;
    private final Integer designComponentId;
    private final String title;
    private final String previewImageName;
    private final String userEmail;
    private final LocalDateTime createdAt;
    private final Long prefers;

    @QueryProjection
    public Preview(Long postId, Integer designComponentId, String title, String previewImageName,
        String userEmail, LocalDateTime createdAt, Long prefers) {
      this.postId = postId;
      this.designComponentId = designComponentId;
      this.title = title;
      this.previewImageName = previewImageName;
      this.userEmail = userEmail;
      this.createdAt = createdAt;
      this.prefers = prefers;
    }
  }
}
