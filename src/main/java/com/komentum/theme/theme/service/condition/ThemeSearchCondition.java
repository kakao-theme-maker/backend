package com.komentum.theme.theme.service.condition;

import lombok.Getter;

@Getter
public class ThemeSearchCondition {

  private boolean bookmarked;

  public void withBookmarked(Boolean bookmarked) {
    this.bookmarked = bookmarked;
  }
}
