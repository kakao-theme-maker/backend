package com.komentum.post.domain;

import com.komentum.post.dto.TagDto.TagUpdateDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tag {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long tagId;

  @Column
  private String tagName;

  @ManyToOne
  private Post post;

  public static Tag createTransient(String tagName, Post post) {
    return Tag.builder()
        .tagName(tagName)
        .post(post).build();
  }

  public void update(TagUpdateDto updateDto) {
    if (tagName != null) {
      this.tagName = updateDto.getTagName();
    }
  }
}
