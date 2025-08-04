package com.komentum.post.domain;

import com.komentum.domain.User;
import com.komentum.post.dto.CommentDto.CommentCreateDto;
import com.komentum.post.dto.CommentDto.CommentUpdateDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long commentId;

  @ManyToOne
  private Post post;

  @ManyToOne
  private User user;

  @Column
  private String content;

  @CreatedDate
  private LocalDateTime createdAt;

  @LastModifiedDate
  private LocalDateTime updatedAt;

  public static Comment createTransient(CommentCreateDto dto, Post post, User user) {
    return Comment.builder()
        .post(post)
        .user(user)
        .content(dto.getContent())
        .build();
  }

  public void update(CommentUpdateDto dto) {
    if (dto.getContent() != null) {
      this.content = dto.getContent();
    }
  }
}
