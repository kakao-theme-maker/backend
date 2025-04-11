package com.theme.controller;

import com.theme.domain.ComponentType;
import com.theme.service.ComponentTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/component-types")
public class ComponentTypeController {

    private final ComponentTypeService componentTypeService;

    @Autowired
    public ComponentTypeController(ComponentTypeService componentTypeService) {
        this.componentTypeService = componentTypeService;
    }

    @PostMapping
    public ResponseEntity<ComponentType> createComponentType(@RequestBody ComponentType componentType) {
        return ResponseEntity.ok(componentTypeService.createComponentType(componentType));
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
    public ResponseEntity<ComponentType> updateComponentType(@PathVariable("id") Integer id, @RequestBody ComponentType componentTypeDetails) {
        return ResponseEntity.ok(componentTypeService.updateComponentType(id, componentTypeDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComponentType(@PathVariable("id") Integer id) {
        componentTypeService.deleteComponentType(id);
        return ResponseEntity.ok().build();
    }
}
