package com.komentum.test.fixture.post;

import com.komentum.designcomponent.domain.ComponentType;
import com.komentum.designcomponent.domain.DesignComponent;
import com.komentum.post.domain.DesignBoard;
import com.komentum.post.domain.Post;

public final class DesignBoardFixture {

  private DesignBoardFixture() {
  }

  /**
   * 게시글·에셋·타입을 연결하는 미저장 매핑을 생성한다.
   * 매퍼 단위 테스트에서는 게시글을 null로 전달할 수 있다.
   */
  public static DesignBoard designBoard(Post post, DesignComponent component,
      ComponentType componentType) {
    return DesignBoard.builder()
        .post(post)
        .designComponent(component)
        .componentType(componentType)
        .build();
  }
}
