package com.komentum.theme.component.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "design_component_component_type", uniqueConstraints = {
    @UniqueConstraint(name = "uk_design_component_component_type", columnNames = {
        "design_component_id", "component_type_id"
    })
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DesignComponentComponentType {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer designComponentComponentTypeId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "design_component_id", nullable = false)
  private DesignComponent designComponent;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "component_type_id", nullable = false)
  private ComponentType componentType;

  public static DesignComponentComponentType of(DesignComponent designComponent,
      ComponentType componentType) {
    return DesignComponentComponentType.builder()
        .designComponent(designComponent)
        .componentType(componentType)
        .build();
  }
}
