package com.theme.dto;

import lombok.Data;

@Data
public class CreateDesignComponentRequest {

    private String userEmail;

    private Integer componentTypeId; // Component Type ID

    private String imageUrl;

    private String rgba;

    private Boolean isPublic;
}
