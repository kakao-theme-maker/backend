package com.komentum.designcomponent.domain;

import com.komentum.global.enums.FileExtension;
import com.komentum.designcomponent.enums.Platform;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "platform_component_type")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformComponentType {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long platformComponentTypeId;

  @Column(nullable = false)
  private String path;

  @Column(nullable = false)
  private Integer width;

  @Column(nullable = false)
  private Integer height;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private FileExtension fileExtension;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private Platform platform;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "component_type_id")
  private ComponentType componentType;
}
