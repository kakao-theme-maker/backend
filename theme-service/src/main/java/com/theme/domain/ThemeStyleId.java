package com.theme.domain;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThemeStyleId implements Serializable {

    @Column(name = "theme_component_id")
    private Integer themeComponentId;

    @Column(name = "color_type_id")
    private Integer colorTypeId;
}
