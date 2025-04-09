package com.theme.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "theme_component")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThemeComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본 키 생성: 데이터베이스가 자동으로 값을 생성한다.
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

//    @Column(name = "created_at")
//    private LocalDateTime createdAt;
//
//    @Column(name = "updated_at")
//    private LocalDateTime updatedAt;

    //관계 매핑
    @OneToMany(mappedBy = "themeComponent", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ThemeStyle> themeStyles = new HashSet<>();

    @OneToMany(mappedBy = "themeComponent", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ThemeImage> themeImages = new HashSet<>();

//    @PrePersist //엔터티가 저장되기 전에 호출되어 생성 시간, 수정 시간 설정.
//    protected void onCreate() {
//        createdAt = LocalDateTime.now();
//        updatedAt = LocalDateTime.now();
//    }
//
//    @PreUpdate // 수정 시간 갱신
//    protected void onUpdate() {
//        updatedAt = LocalDateTime.now();
//    }
}
