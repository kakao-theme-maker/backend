package com.komentum.post.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.komentum.post.domain.Tag;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class TagDto {

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TagBatchCreateDto {

    List<TagCreateDto> tagNames;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TagCreateDto {

    @JsonProperty("tag_name")
    String tagName;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TagUpdateDto {

    String tagName;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TagResponse {

    @JsonProperty("tag_id")
    Long tagId;

    @JsonProperty("tag_name")
    String tagName;

    public static TagResponse from(Tag tag) {
      return TagResponse.builder().tagId(tag.getTagId()).tagName(tag.getTagName()).build();
    }
  }
}
