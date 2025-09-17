package com.komentum.theme.theme.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.BatchSize;

@Slf4j
@Entity
@Table(name = "theme_component")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThemeComponent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "theme_component_id")
  private Integer themeComponentId;

  @Column(name = "userEmail", nullable = false)
  private String userEmail;

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
