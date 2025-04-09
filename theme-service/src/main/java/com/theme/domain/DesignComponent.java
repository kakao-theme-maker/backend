package com.theme.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "design_component")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DesignComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer designComponentId;

    private String userEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_type_id")
    private ComponentType componentType; // 연관 관계 설정

    private String imageUrl;

    private String rgba;

    private Boolean isPublic;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
