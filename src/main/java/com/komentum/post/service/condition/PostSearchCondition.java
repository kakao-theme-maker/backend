package com.komentum.post.service.condition;

import com.komentum.designcomponent.enums.TypeCode;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PostSearchCondition {

  private String authorPublicId;
  private String keyword;
  private TypeCode typeCode;
  private List<Long> pinnedPostIds = new ArrayList<>();

  public PostSearchCondition withAuthorPublicId(String authorPublicId) {
    this.authorPublicId = authorPublicId;
    return this;
  }

  public PostSearchCondition withKeyword(String keyword) {
    this.keyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
    return this;
  }

  public PostSearchCondition withTypeCode(TypeCode typeCode) {
    this.typeCode = typeCode;
    return this;
  }

  public PostSearchCondition withPinnedPostIds(List<Long> pinnedPostIds) {
    this.pinnedPostIds = pinnedPostIds;
    return this;
  }
}
