package com.komentum.theme.component.domain;

import com.komentum.theme.component.dto.ComponentTypeUpdateRequest;
import com.komentum.theme.component.enums.TypeCode;
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
  private TypeCode typeCode;

  @Column(nullable = false)
  private String name;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  public void update(ComponentTypeUpdateRequest componentType) {
    if (componentType.getExplain() != null) {
      this.explain = componentType.getExplain();
      this.name = componentType.getName();
      this.typeCode = componentType.getTypeCode();
    }
  }

  public void replace(ComponentType componentType) {
    this.explain = componentType.getExplain();
    this.typeCode = componentType.getTypeCode();
  }

  public boolean isSame(ComponentType other) {
    if (other == null) {
      return false;
    }
    return Objects.equals(this.explain, other.explain)
        && this.typeCode.equals(other.typeCode);
  }
}
