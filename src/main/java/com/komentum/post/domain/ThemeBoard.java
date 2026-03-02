package com.komentum.post.domain;

import com.komentum.theme.theme.domain.ThemeComponent;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThemeBoard {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long themeBoardId;

  @OneToOne
  @JoinColumn(name = "post_id", nullable = false, unique = true)
  private Post post;

  @OneToOne
  @JoinColumn(name = "theme_component_id", nullable = false, unique = true)
  private ThemeComponent themeComponent;

  public Long findPostId() {
    return post.getPostId();
  }
}
