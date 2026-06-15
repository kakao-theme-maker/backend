package com.komentum.theme.component.domain;

import com.komentum.global.enums.FileExtension;
import com.komentum.theme.component.enums.Platform;
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

  // seed 문서 내에서 사용되는 식별자 ( upsert 시 데이터 존재 유무 확인 및 데이터 구분을 위해 사용 )
  @Column(nullable = false, unique = true)
  private String code;

  /**
   * platformComponentType을 파라미터의 값으로 완전히 대체한다 ( code값은 문자열 식별자이므로 제외 )
   * @param platformComponentType 대체할 platformComponentType
   * */
  public void replace(PlatformComponentType platformComponentType) {
    this.path = platformComponentType.getPath();
    this.width = platformComponentType.getWidth();
    this.height = platformComponentType.getHeight();
    this.fileExtension = platformComponentType.getFileExtension();
    this.platform = platformComponentType.getPlatform();
  }
}
