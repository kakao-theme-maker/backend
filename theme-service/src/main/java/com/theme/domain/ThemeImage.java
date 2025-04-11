package com.theme.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "theme_image")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThemeImage {

    @EmbeddedId //복합키를 나타내는 어노테이션
    private ThemeImageId id; //기본키

    @ManyToOne(fetch = FetchType.LAZY) //다대일 관계를 나타냄 fetch = FetchType.LAZY : 연관된 데이터를 필요할 때만 로드
    @MapsId("themeComponentId")
    @JoinColumn(name = "theme_component_id") // DB에서 외래 키 칼럼 이름을 지정
    private ThemeComponent themeComponent;
}
