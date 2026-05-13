package com.komentum.theme.component.domain;

import com.komentum.user.domain.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "design_component")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DesignComponent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer designComponentId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  private String imageUrl;

  @Column(name = "is_public")
  private Boolean isPublic;

  @Builder.Default
  @OneToMany(mappedBy = "designComponent", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<DesignComponentComponentType> componentTypeMappings = new ArrayList<>();

  @CreationTimestamp
  @Column(updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  private LocalDateTime updatedAt;

  public void update(String imageUrl, Boolean isPublic) {
    if (imageUrl != null) {
      this.imageUrl = imageUrl;
    }
    if (isPublic != null) {
      this.isPublic = isPublic;
    }
  }

  public List<ComponentType> getComponentTypes() {
    return componentTypeMappings.stream()
        .map(DesignComponentComponentType::getComponentType)
        .toList();
  }

  public void replaceComponentTypes(List<ComponentType> componentTypes) {
    this.componentTypeMappings.clear();
    if (componentTypes == null) {
      return;
    }
    for (ComponentType componentType : componentTypes) {
      this.componentTypeMappings.add(DesignComponentComponentType.of(this, componentType));
    }
  }
}
