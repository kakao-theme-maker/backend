package com.komentum.post.dto.query;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class DesignBoardQuery {

  @Getter
  @NoArgsConstructor
  public static class Detail {

    private Long postId;
    private String title;
    private String content;
    private Integer designComponentId;
    private String userEmail;
    private String userName;
    private LocalDateTime createdAt;
    private String previewImageName;
    private Long prefers;
    private Long comments;
    private boolean liked;
    private boolean bookmarked;
    private String profileImage;

    @QueryProjection
    public Detail(
        Long postId,
        String title,
        String content,
        Integer designComponentId,
        String userEmail,
        String userName,
        LocalDateTime createdAt,
        String previewImageName,
        Long prefers,
        Long comments,
        boolean liked,
        boolean bookmarked,
        String profileImage) {
      this.postId = postId;
      this.title = title;
      this.content = content;
      this.designComponentId = designComponentId;
      this.userEmail = userEmail;
      this.userName = userName;
      this.createdAt = createdAt;
      this.previewImageName = previewImageName;
      this.prefers = prefers;
      this.comments = comments;
      this.liked = liked;
      this.bookmarked = bookmarked;
      this.profileImage = profileImage;
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
