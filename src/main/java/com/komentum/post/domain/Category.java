package com.komentum.post.domain;

import com.komentum.post.dto.CategoryDto.CategoryCreateDto;
import com.komentum.post.dto.CategoryDto.CategoryUpdateDto;
import com.komentum.post.service.enums.CategoryType;
import com.komentum.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long categoryId;

  @ManyToOne
  @JoinColumn(name = "owner_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  User owner; // owner

  @Column(nullable = false)
  String name; // category name

  @Builder.Default
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  CategoryType categoryType = CategoryType.CUSTOM;

  public static Category createTransient(User owner, CategoryCreateDto createDto) {
    return Category.builder()
        .name(createDto.getName())
        .owner(owner)
        .build();
  }

  public void update(CategoryUpdateDto updateDto) {
    this.name = updateDto.getName();
  }

  public boolean isOwner(User user) {
    return this.owner.getUserEmail().equals(user.getUserEmail());
  }
}
