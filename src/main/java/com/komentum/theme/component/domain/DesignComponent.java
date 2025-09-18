package com.komentum.theme.component.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "design_component")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DesignComponent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer designComponentId;

  private String userEmail;

  private String imageUrl;

  @Column(name = "is_public")
  private Boolean isPublic;

  @CreationTimestamp
  @Column(updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  private LocalDateTime updatedAt;
}
