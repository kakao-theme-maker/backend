package com.komentum.theme.core.service.condition;

import lombok.Getter;

@Getter
public class ThemeSearchCondition {

  private Boolean bookmarked = false;

  public void withBookmarked(Boolean bookmarked) {
    this.bookmarked = bookmarked;
  }
}
