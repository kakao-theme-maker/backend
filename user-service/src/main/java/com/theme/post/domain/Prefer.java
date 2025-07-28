package com.theme.post.domain;

import com.theme.domain.User;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Prefer {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long preferId;

  @ManyToOne
  private Post post;

  @ManyToOne
  private User user;

  public static Prefer createTransient(Post post, User user) {
    return Prefer.builder().post(post).user(user).build();
  }
}
