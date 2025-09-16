package com.komentum.theme.component.controller;

import com.komentum.theme.component.domain.ComponentType;
import com.komentum.theme.component.dto.CreateComponentTypeRequest;
import com.komentum.theme.component.dto.UpdateComponentTypeRequest;
import com.komentum.theme.component.service.ComponentTypeService;
import jakarta.validation.Valid;
import java.util.List;
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
  public ResponseEntity<ComponentType> createComponentType(
      @Valid @RequestBody CreateComponentTypeRequest request) {
    return ResponseEntity.ok(componentTypeService.createComponentType(request));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ComponentType> getComponentTypeById(@PathVariable("id") Integer id) {
    return ResponseEntity.ok(componentTypeService.getComponentTypeById(id));
  }

  @GetMapping
  public ResponseEntity<List<ComponentType>> getAllComponentTypes() {
    return ResponseEntity.ok(componentTypeService.getAllComponentTypes());
  }

  @PutMapping("/{id}")
  public ResponseEntity<ComponentType> updateComponentType(@PathVariable("id") Integer id,
      @Valid @RequestBody UpdateComponentTypeRequest request) {
    return ResponseEntity.ok(componentTypeService.updateComponentType(id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteComponentType(@PathVariable("id") Integer id) {
    componentTypeService.deleteComponentType(id);
    return ResponseEntity.ok().build();
  }
}
