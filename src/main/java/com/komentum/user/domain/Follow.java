package com.komentum.user.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(
    name = "user_follow",
    indexes = {
        @Index(name = "idx_user_follow_followee_id", columnList = "followee_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_user_follow_follower_followee",
            columnNames = {"follower_id", "followee_id"}
        )
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Follow {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long followId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "follower_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private User follower;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "followee_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private User followee;

  @CreatedDate
  private LocalDateTime followedAt;

  private Follow(User follower, User followee) {
    this.follower = follower;
    this.followee = followee;
  }

  public static Follow createTransient(User follower, User followee) {
    return new Follow(follower, followee);
  }
}
