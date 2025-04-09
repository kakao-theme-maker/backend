package com.theme.domain;

import jakarta.persistence.*; //JPA 어노테이션 사용하기 위해 임포트
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity // JPA 엔티티 선언
@Table(name = "color_style") //color_style과 매핑
@Data
@Builder // 빌더 패턴 사용하여 객체 생성할 수 있도록 함.
@NoArgsConstructor // 어떠한 변수도 사용하지 않는 기본 생성자를 자동완성 시켜주는 어노테이션
@AllArgsConstructor // 모든 필드를 초기화하는 생성자를 자동완성 시켜주는 어노테이션
public class ColorStyle {

    @Id //PK
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "color_type_id")
    private Integer colorTypeId;


    @Column(name = "`explain`")
    private String explain;

    @Column(name = "ios_style_name")
    private String iosStyleName;

    @Column(name = "ios_style_props")
    private String iosStyleProps;

    @Column(name = "android_style_name")
    private String androidStyleName;
}
