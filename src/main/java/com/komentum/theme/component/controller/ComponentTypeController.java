package com.komentum.theme.component.controller;

import com.komentum.theme.component.dto.ComponentTypeDto;
import com.komentum.theme.component.dto.CreateComponentTypeRequest;
import com.komentum.theme.component.dto.UpdateComponentTypeRequest;
import com.komentum.theme.component.service.ComponentTypeService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/component-types")
public class ComponentTypeController {

  private final ComponentTypeService componentTypeService;

  @Autowired
  public ComponentTypeController(ComponentTypeService componentTypeService) {
    this.componentTypeService = componentTypeService;
  }

  @PostMapping
  public ResponseEntity<ComponentTypeDto> createComponentType(
      @Valid @RequestBody CreateComponentTypeRequest request) {
    var createdComponentType = componentTypeService.createComponentType(request);
    return ResponseEntity.ok(ComponentTypeDto.from(createdComponentType));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ComponentTypeDto> getComponentTypeById(@PathVariable("id") Integer id) {
    var componentType = componentTypeService.getComponentTypeById(id);
    return ResponseEntity.ok(ComponentTypeDto.from(componentType));
  }

  @GetMapping
  public ResponseEntity<List<ComponentTypeDto>> getAllComponentTypes() {
    var componentTypes = componentTypeService.getAllComponentTypes()
        .stream()
        .map(ComponentTypeDto::from)
        .collect(Collectors.toList());
    return ResponseEntity.ok(componentTypes);
  }

  @PutMapping("/{id}")
  public ResponseEntity<ComponentTypeDto> updateComponentType(@PathVariable("id") Integer id,
      @Valid @RequestBody UpdateComponentTypeRequest request) {
    var updatedComponentType = componentTypeService.updateComponentType(id, request);
    return ResponseEntity.ok(ComponentTypeDto.from(updatedComponentType));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteComponentType(@PathVariable("id") Integer id) {
    componentTypeService.deleteComponentType(id);
    return ResponseEntity.ok().build();
  }
}
