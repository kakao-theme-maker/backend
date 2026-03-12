package com.komentum.post.domain;

import com.komentum.theme.theme.domain.ThemeComponent;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"post_id", "theme_component_id"}
        )
    }
)
public class ThemeBoard {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long themeBoardId;

  @OneToOne
  @JoinColumn(name = "post_id", nullable = false)
  private Post post;

  @OneToOne
  @JoinColumn(name = "theme_component_id", nullable = false)
  private ThemeComponent themeComponent;

  public Long findPostId() {
    return post.getPostId();
  }
}
