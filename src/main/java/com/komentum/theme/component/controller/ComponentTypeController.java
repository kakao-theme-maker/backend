package com.komentum.theme.component.controller;

import com.komentum.theme.component.domain.ComponentType;
import com.komentum.theme.component.dto.ComponentTypeCreateRequest;
import com.komentum.theme.component.dto.ComponentTypeDto;
import com.komentum.theme.component.dto.ComponentTypeUpdateRequest;
import com.komentum.theme.component.dto.SeedResult;
import com.komentum.theme.component.mapper.ComponentTypeMapper;
import com.komentum.theme.component.service.ComponentTypeSeeder;
import com.komentum.theme.component.service.ComponentTypeService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/component-types")
@RequiredArgsConstructor
public class ComponentTypeController {

  private final ComponentTypeService componentTypeService;
  private final ComponentTypeSeeder componentTypeSeeder;
  private final ComponentTypeMapper componentTypeMapper;

  @PostMapping
  @Operation(summary = "Admin 사용자가 새로운 component type 생성")
  public ResponseEntity<ComponentTypeDto> createComponentType(
      @Valid @RequestBody ComponentTypeCreateRequest request) {
    ComponentType createdComponentType = componentTypeService.createComponentType(request);
    return ResponseEntity.ok(componentTypeMapper.toComponentTypeDto(createdComponentType));
  }

  @GetMapping("/{component_type_id}")
  @Operation(summary = "인증된 사용자가 ID=component_type_id인 component type 조회")
  public ResponseEntity<ComponentTypeDto> getComponentTypeById(
      @PathVariable("component_type_id") Integer componentTypeId) {
    ComponentType componentType = componentTypeService.getComponentTypeById(componentTypeId);
    return ResponseEntity.ok(componentTypeMapper.toComponentTypeDto(componentType));
  }

  @GetMapping
  @Operation(summary = "인증된 사용자가 모든 component type 조회")
  public ResponseEntity<List<ComponentTypeDto>> getAllComponentTypes() {
    List<ComponentTypeDto> componentTypes = componentTypeService.getAllComponentTypes()
        .stream()
        .map(componentTypeMapper::toComponentTypeDto)
        .collect(Collectors.toList());
    return ResponseEntity.ok(componentTypes);
  }

  @PutMapping("/{component_type_id}")
  @Operation(summary = "Admin 사용자가 ID=component_type_id인 component type 수정")
  public ResponseEntity<ComponentTypeDto> updateComponentType(
      @PathVariable("component_type_id") Integer componentTypeId,
      @Valid @RequestBody ComponentTypeUpdateRequest request) {
    ComponentType updatedComponentType = componentTypeService.updateComponentType(componentTypeId,
        request);
    return ResponseEntity.ok(componentTypeMapper.toComponentTypeDto(updatedComponentType));
  }

  @PutMapping("/seed")
  @Operation(summary = "Admin 사용자가 시드 데이터를 기반으로 component type 정보를 수정/삽입한다")
  public ResponseEntity<SeedResult> upsertComponentTypeWithSeed() {
    SeedResult result = componentTypeSeeder.upsertComponentType();
    return ResponseEntity.ok(result);
  }
}
