package com.komentum.test.fixture.post;

import com.komentum.post.domain.Post;
import com.komentum.post.domain.enums.PostType;
import com.komentum.user.domain.User;

public final class PostFixture {

  private PostFixture() {
  }

  /**
   * 작성자·제목·유형과 기본 본문을 가진 미저장 게시글을 생성한다.
   */
  public static Post post(User owner, String title, PostType postType) {
    return Post.builder()
        .user(owner)
        .title(title)
        .content("content")
        .postType(postType)
        .build();
  }
}
