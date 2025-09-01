package com.komentum.theme.theme.domain;

import com.komentum.theme.component.domain.DesignComponent;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "theme_image")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThemeImage {

  @EmbeddedId //복합키를 나타내는 어노테이션
  private ThemeImageId id; //기본키

  @ToString.Exclude
  @ManyToOne(fetch = FetchType.LAZY) //다대일 관계를 나타냄 fetch = FetchType.LAZY : 연관된 데이터를 필요할 때만 로드
  @MapsId("themeComponentId")
  @JoinColumn(name = "theme_component_id") // DB에서 외래 키 칼럼 이름을 지정
  private ThemeComponent themeComponent;

  @ToString.Exclude
  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("designComponentId")
  @JoinColumn(name = "design_component_id")
  private DesignComponent designComponent;
}
