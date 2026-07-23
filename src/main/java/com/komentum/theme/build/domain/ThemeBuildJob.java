package com.komentum.theme.build.domain;

import com.komentum.designcomponent.enums.Platform;
import com.komentum.theme.core.domain.ThemeComponent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor
@Table(
    name = "theme_build_job",
    indexes = {
        @Index(
            name = "idx_theme_build_job_theme_platform_status_created",
            columnList = "theme_component_id, platform, status, created_at"
        )
    }
)
@EntityListeners(AuditingEntityListener.class)
public class ThemeBuildJob {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "build_id")
  private Long buildId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "theme_component_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private ThemeComponent themeComponent;

  @Enumerated(EnumType.STRING)
  @Column(name = "platform", nullable = false)
  private Platform platform;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private ThemeBuildStatus status;

  @Column(name = "package_url", length = 1024)
  private String packageUrl;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  private ThemeBuildJob(ThemeComponent themeComponent, Platform platform) {
    this.themeComponent = themeComponent;
    this.platform = platform;
    this.status = ThemeBuildStatus.RUNNING;
  }

  public static ThemeBuildJob createRunning(
      ThemeComponent themeComponent,
      Platform platform
  ) {
    return new ThemeBuildJob(themeComponent, platform);
  }
}
