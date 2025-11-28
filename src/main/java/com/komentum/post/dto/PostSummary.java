package com.komentum.post.dto;

import com.komentum.post.domain.Post;
import com.komentum.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// dto 인식 오류로 PostDetailProjection은 별도의 클래스로 분리
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostSummary {

  private Post post;
  private User author;
  private Long prefers;

  public Long findPostId() {
    return post.getPostId();
  }

  public String findPreviewImageName() {
    return post.getPreviewImageName();
  }
}
