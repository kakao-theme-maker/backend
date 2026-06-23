package com.komentum.designcomponent.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SeedResult {

  private int created;
  private int updated;
}
