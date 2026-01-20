package com.komentum.post.domain;

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
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(
    name = "category_post",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"category_id", "post_id"}
    )
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryPost {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long categoryPostId;

  @ManyToOne
  @JoinColumn(name = "category_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  Category category;

  @ManyToOne
  @JoinColumn(name = "post_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  Post post;
}
