package com.komentum.theme.component.domain;

import com.komentum.theme.component.enums.Platform;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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

  @OneToMany(mappedBy = "componentType", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  @Builder.Default
  private List<DesignComponent> designComponents = new ArrayList<>();

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Deprecated
  public String getIosComponentPath() {
    if (platform == Platform.IOS) {
      return componentPath;
    }
    return null;
  }

  @Deprecated
  public String getAndroidComponentPath() {
    if (platform == Platform.ANDROID) {
      return componentPath;
    }
    return null;
  }

  @Deprecated
  public String getIosComponentName() {
    if (platform == Platform.IOS) {
      return componentName;
    }
    return null;
  }

  @Deprecated
  public String getAndroidComponentName() {
    if (platform == Platform.ANDROID) {
      return componentName;
    }
    return null;
  }

  public static class ComponentTypeBuilder {
    @Deprecated
    public ComponentTypeBuilder iosComponentPath(String iosComponentPath) {
      this.platform = Platform.IOS;
      this.componentPath = iosComponentPath;
      return this;
    }

    @Deprecated
    public ComponentTypeBuilder androidComponentPath(String androidComponentPath) {
      this.platform = Platform.ANDROID;
      this.componentPath = androidComponentPath;
      return this;
    }

    @Deprecated
    public ComponentTypeBuilder iosComponentName(String iosComponentName) {
      if (this.platform == null) {
        this.platform = Platform.IOS;
      }
      this.componentName = iosComponentName;
      return this;
    }

    @Deprecated
    public ComponentTypeBuilder androidComponentName(String androidComponentName) {
      if (this.platform == null) {
        this.platform = Platform.ANDROID;
      }
      this.componentName = androidComponentName;
      return this;
    }
  }
}
