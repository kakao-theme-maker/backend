package com.komentum.theme.component.domain;

import com.komentum.theme.component.enums.Platform;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "component_type")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComponentType {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "component_type_id")
  private Integer componentTypeId;

  @Column(name = "`explain`")
  private String explain;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Platform platform;

  @Column(name = "component_path", nullable = false)
  private String componentPath;

  @Column(name = "component_name", nullable = false)
  private String componentName;

  @Column(name = "size_x")
  private Integer sizeX;

  @Column(name = "size_y")
  private Integer sizeY;


  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;


  public static class ComponentTypeBuilder {
  }
}
