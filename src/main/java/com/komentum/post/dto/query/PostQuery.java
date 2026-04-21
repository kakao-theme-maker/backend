package com.komentum.post.dto.query;

import com.komentum.post.domain.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public class PostQuery {

  @Getter
  @Setter
  @Builder
  @AllArgsConstructor
  public static class Detail {

    private Post post;
    private Long prefers;
    private Long comments;
    private Boolean preferred;
    private Boolean bookmarked;
  }
}
