package com.komentum.theme.component.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "color_style")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColorStyle {

  @Id //PK
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "color_type_id")
  private Integer colorTypeId;

  @Column(name = "`explain`")
  private String explain;

  @Column(name = "ios_style_name")
  private String iosStyleName;

  @Column(name = "android_style_name")
  private String androidStyleName;
}
