package com.komentum.post.domain;

import com.komentum.post.domain.enums.PostType;
import com.komentum.post.dto.PostDto.PostCreateDto;
import com.komentum.post.dto.PostDto.PostUpdateDto;
import com.komentum.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long postId;

  @ManyToOne
  @JoinColumn(name = "user_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private User user;

  @Column
  private String title;

  @Column(columnDefinition = "text")
  private String content;

  @Column(nullable = true)
  private String previewImageName;

  @CreatedDate
  private LocalDateTime createdAt;

  @LastModifiedDate
  private LocalDateTime updatedAt;

  @Enumerated
  @Column(nullable = false)
  private PostType postType;

  public static Post createTransient(
      PostCreateDto createDto,
      User user,
      String previewImageName,
      PostType postType) {
    return Post.builder()
        .title(createDto.getTitle())
        .user(user)
        .postType(postType)
        .previewImageName(previewImageName)
        .content(createDto.getContent()).build();
  }

  public void update(PostUpdateDto updateDto) {
    if (updateDto.getTitle() != null) {
      this.title = updateDto.getTitle();
    }
    if (updateDto.getContent() != null) {
      this.content = updateDto.getContent();
    }
    if (updateDto.getPreviewImageName() != null) {
      this.previewImageName = updateDto.getPreviewImageName();
    }
  }
}
