package com.komentum.designcomponent.domain;

import com.komentum.designcomponent.dto.ComponentTypeUpdateRequest;
import com.komentum.designcomponent.enums.PlatformScope;
import com.komentum.designcomponent.enums.TypeCode;
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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "component_type")
@Getter
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
  private TypeCode typeCode;

  @Column(nullable = false)
  private String name;

  /**
   * 이 component type이 공통으로 사용되는지, 특정 플랫폼 전용인지를 나타낸다.
   */
  @Enumerated(EnumType.STRING)
  @Builder.Default
  @Column(name = "platform_scope", nullable = false)
  private PlatformScope platformScope = PlatformScope.COMMON;

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
    if (componentType.getTypeCode() != null) {
      this.typeCode = componentType.getTypeCode();
    }
    if (componentType.getName() != null) {
      this.name = componentType.getName();
    }
    if (componentType.getPlatformScope() != null) {
      this.platformScope = componentType.getPlatformScope();
    }
  }

  public void replace(ComponentType componentType) {
    this.explain = componentType.getExplain();
    this.name = componentType.getName();
    this.typeCode = componentType.getTypeCode();
    this.platformScope = componentType.getPlatformScope();
  }

  public boolean isSame(ComponentType other) {
    if (other == null) {
      return false;
    }
    return Objects.equals(this.explain, other.explain)
        && this.typeCode.equals(other.typeCode)
        && this.name.equals(other.name)
        && this.platformScope == other.platformScope;
  }
}
