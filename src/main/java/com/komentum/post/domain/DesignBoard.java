package com.komentum.post.domain;

import com.komentum.designcomponent.domain.DesignComponent;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
    uniqueConstraints = @UniqueConstraint(
        name = "unique_design_board",
        columnNames = {"post_id", "design_component_id"}
    )
)
public class DesignBoard {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long designBoardId;

  @ManyToOne
  @JoinColumn(nullable = false, name = "post_id")
  private Post post;

  @ManyToOne
  @JoinColumn(nullable = false, name = "design_component_id")
  private DesignComponent designComponent;
}
