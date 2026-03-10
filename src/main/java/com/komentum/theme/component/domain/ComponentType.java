package com.komentum.theme.component.domain;

import com.komentum.theme.component.dto.ComponentTypeUpdateRequest;
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
import java.util.Objects;
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

  public void update(ComponentTypeUpdateRequest componentType) {
    if (componentType.getExplain() != null) {
      this.explain = componentType.getExplain();
    }
    if (componentType.getPlatform() != null) {
      this.platform = componentType.getPlatform();
    }
    if (componentType.getComponentName() != null) {
      this.componentName = componentType.getComponentName();
    }
    if (componentType.getComponentPath() != null) {
      this.componentPath = componentType.getComponentPath();
    }
    if (componentType.getSizeX() != null) {
      this.sizeX = componentType.getSizeX();
    }
    if (componentType.getSizeY() != null) {
      this.sizeY = componentType.getSizeY();
    }
  }

  public void replace(ComponentType componentType) {
    this.explain = componentType.getExplain();
    this.platform = componentType.getPlatform();
    this.componentName = componentType.getComponentName();
    this.componentPath = componentType.getComponentPath();
    this.sizeX = componentType.getSizeX();
    this.sizeY = componentType.getSizeY();
    this.createdAt = componentType.getCreatedAt();
    this.updatedAt = componentType.getUpdatedAt();
  }

  public boolean isSame(ComponentType other) {
    if (other == null) {
      return false;
    }
    return Objects.equals(this.explain, other.explain)
        && this.platform == other.platform
        && Objects.equals(this.componentPath, other.componentPath)
        && Objects.equals(this.componentName, other.componentName)
        && Objects.equals(this.sizeX, other.sizeX)
        && Objects.equals(this.sizeY, other.sizeY);
  }
}
