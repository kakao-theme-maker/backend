package com.komentum.theme.core.domain;

import com.komentum.theme.core.dto.ThemeUpdateRequest;
import com.komentum.theme.core.enums.ThemeType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Slf4j
@Entity
@Table(name = "theme_component")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ThemeComponent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "theme_component_id")
  private Integer themeComponentId;

  @Column(name = "userEmail", nullable = false)
  private String userEmail;

  @Column(name = "themeCode", nullable = false, unique = true)
  private String themeCode;

  @Builder.Default
  @Column(name = "themeType", nullable = false)
  @Enumerated(EnumType.STRING)
  private ThemeType themeType = ThemeType.USER;

  @Column(name = "theme_name", nullable = false)
  private String themeName;

  @Column(name = "version_number", nullable = false)
  private String versionNumber;

  @Column(name = "version_name")
  private String versionName;

  @Column(name = "is_done")
  private Boolean isDone;

  @Column(name = "is_public")
  private Boolean isPublic;

  @CreatedDate
  private LocalDateTime createdAt;

  @LastModifiedDate
  private LocalDateTime updatedAt;

  @Builder.Default
  @Setter(AccessLevel.NONE)
  @BatchSize(size = 100)
  @OneToMany(mappedBy = "themeComponent", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<ThemeImage> themeImages = new HashSet<>();

  @Builder.Default
  @Setter(AccessLevel.NONE)
  @BatchSize(size = 100)
  @OneToMany(mappedBy = "themeComponent", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<ThemeStyle> themeStyles = new HashSet<>();

  @PrePersist
  void prePersist() {
    if (themeCode == null) {
      themeCode = UUID.randomUUID().toString();
    }
  }

  public void update(ThemeUpdateRequest updateRequest) {
    this.themeName = updateRequest.getThemeName();
  }

  public void addThemeImage(ThemeImage themeImage) {
    boolean alreadyExists = this.themeImages.stream()
        .anyMatch(existingImage ->
            existingImage.getComponentType().getComponentTypeId()
                .equals(themeImage.getComponentType().getComponentTypeId())
        );
    if (alreadyExists) {
      throw new IllegalArgumentException(
          "Image already exists : " + themeImage.getComponentType().getComponentTypeId());
    }
    this.themeImages.add(themeImage);
    themeImage.setThemeComponent(this);
  }

  public void addThemeStyle(ThemeStyle themeStyle) {
    boolean alreadyExists = this.themeStyles.stream()
        .anyMatch(existingStyle -> existingStyle.getColorStyle().getColorStyleId()
            .equals(themeStyle.getColorStyle().getColorStyleId()));
    if (alreadyExists) {
      throw new IllegalArgumentException(
          "Style already exists : " + themeStyle.getColorStyle().getColorStyleId());
    }
    this.themeStyles.add(themeStyle);
    themeStyle.setThemeComponent(this);
  }
}
