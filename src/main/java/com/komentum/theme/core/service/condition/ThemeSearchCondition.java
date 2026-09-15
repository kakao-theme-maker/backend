package com.komentum.theme.core.service.condition;

import lombok.Getter;

@Getter
public class ThemeSearchCondition {

  private String publicUserId = null;

  public void withPublicUserId(String publicUserId) {
    this.publicUserId = publicUserId;
  }
}
