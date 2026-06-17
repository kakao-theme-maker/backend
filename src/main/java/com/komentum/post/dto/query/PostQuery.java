package com.komentum.post.dto.query;

import com.komentum.post.domain.enums.PostType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class PostQuery {

  @Getter
  @AllArgsConstructor
  public static class UserPostListRow {

    private Long postId;
    private PostType postType;
    private String title;
    private String content;
    private String previewImageName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String userName;
    private String profileImage;
    private Long prefers;
    private Long comments;
    private boolean preferred;
    private boolean bookmarked;
  }
}
