package com.komentum.post.service.condition;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PostSearchCondition {

  private String authorPublicId;
  private List<Long> pinnedPostIds = new ArrayList<>();

  public PostSearchCondition withAuthorPublicId(String authorPublicId) {
    this.authorPublicId = authorPublicId;
    return this;
  }

  public PostSearchCondition withPinnedPostIds(List<Long> pinnedPostIds) {
    this.pinnedPostIds = pinnedPostIds;
    return this;
  }
}
