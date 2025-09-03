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
import org.hibernate.annotations.BatchSize;

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
    this.themeImages.add(themeImage);
    themeImage.setThemeComponent(this);
  }

  public void addThemeStyle(ThemeStyle themeStyle) {
    this.themeStyles.add(themeStyle);
    themeStyle.setThemeComponent(this);
  }
}
