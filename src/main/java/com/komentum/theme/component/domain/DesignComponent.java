package com.komentum.theme.component.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "design_component")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DesignComponent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer designComponentId;

  private String userEmail;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "component_type_id")
  private ComponentType componentType; // 연관 관계 설정

  private String imageUrl;

  private Boolean isPublic;

  @CreationTimestamp
  @Column(updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  private LocalDateTime updatedAt;
}
