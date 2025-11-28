package com.komentum.post.domain;

import com.komentum.theme.component.domain.DesignComponent;
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
public class DesignBoard {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long designBoardId;

  @OneToOne
  @JoinColumn(nullable = false, unique = true)
  private Post post;

  @OneToOne
  @JoinColumn(nullable = false, unique = true)
  private DesignComponent designComponent;

  public Long findPostId() {
    return this.post.getPostId();
  }
}
