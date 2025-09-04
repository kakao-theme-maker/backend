package com.komentum.theme.component.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

  @Column(name = "ios_component_path")
  private String iosComponentPath;

  @Column(name = "ios_component_name")
  private String iosComponentName;

  @Column(name = "android_component_path")
  private String androidComponentPath;

  @Column(name = "android_component_name")
  private String androidComponentName;

  @Column(name = "sizeX")
  private Integer sizeX;

  @Column(name = "sizeY")
  private Integer sizeY;

  @OneToMany(mappedBy = "componentType", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<DesignComponent> designComponents = new ArrayList<>();

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;
}
