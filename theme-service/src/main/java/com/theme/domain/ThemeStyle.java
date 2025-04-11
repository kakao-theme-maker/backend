package com.theme.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "theme_style")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(ThemeStyleId.class)
public class ThemeStyle {

    @Id
    @Column(name = "theme_component_id")
    private Integer themeComponentId;

    @Id
    @Column(name = "color_type_id")
    private Integer colorTypeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_component_id", insertable = false, updatable = false)
    private ThemeComponent themeComponent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "color_type_id", insertable = false, updatable = false)
    private ColorStyle colorStyle;

    @Column(name = "color")
    private String color;
}
