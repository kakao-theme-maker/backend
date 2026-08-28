package com.komentum.designcomponent.domain;

import com.komentum.designcomponent.enums.Platform;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformColorStyle {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long platformColorStyleId;

  @Column(nullable = false)
  private String resourceGroup;

  @Column(nullable = false)
  private String resourceName;

  @Column(nullable = false)
  private Platform platform;

  @Column(nullable = false, unique = true)
  private String code;

  @Builder.Default
  @Column(nullable = false)
  private Double weight = 1.0;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "color_style_id")
  private ColorStyle colorStyle;

  public void replace(PlatformColorStyle platformColorStyle) {
    this.resourceGroup = platformColorStyle.getResourceGroup();
    this.resourceName = platformColorStyle.getResourceName();
    this.platform = platformColorStyle.getPlatform();
  }
}
