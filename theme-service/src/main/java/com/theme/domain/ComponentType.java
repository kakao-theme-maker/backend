package com.theme.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "component_type")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComponentType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "component_type_id")
    private Integer componentTypeId;

    @Column(name = "`explain`")
    private String explain;

    @Column(name = "ios_component_path")
    private String iosComponentPath;

    @Column(name = "ios_component_name")
    private String iosComponentName;

    @Column(name = "android_component_path")
    private String androidComponentPath;

    @Column(name = "android_component_name")
    private String androidComponentName;

    @Column(name = "sizeX")
    private Integer sizeX;

    @Column(name = "sizeY")
    private Integer sizeY;
}
