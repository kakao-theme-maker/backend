// 복합 키를 처리하기 위해 만듦
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
public class ThemeImageId implements Serializable {

    @Column(name = "theme_component_id")
    private Integer themeComponentId;  //FK

    @Column(name = "design_component_id")
    private Integer designComponentId;
}
