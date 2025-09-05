package com.komentum.theme.component.controller;

import com.komentum.theme.component.domain.DesignComponent;
import com.komentum.theme.component.dto.CreateDesignComponentRequest;
import com.komentum.theme.component.service.DesignComponentService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/design-components")
public class DesignComponentController {

  private final DesignComponentService designComponentService;

  @Autowired
  public DesignComponentController(DesignComponentService designComponentService) {
    this.designComponentService = designComponentService;
  }

  @PostMapping
  public ResponseEntity<DesignComponent> createDesignComponent(
      @RequestBody CreateDesignComponentRequest request
  ) {
    DesignComponent saved = designComponentService.createDesignComponent(request);
    return ResponseEntity.ok(saved);
  }


  @GetMapping("/{id}")
  public ResponseEntity<DesignComponent> getDesignComponentById(@PathVariable("id") Integer id) {
    return ResponseEntity.ok(designComponentService.getDesignComponentById(id));
  }

  @GetMapping
  public ResponseEntity<List<DesignComponent>> getAllDesignComponents() {
    return ResponseEntity.ok(designComponentService.getAllDesignComponents());
  }


  @PutMapping("/{id}")
  public ResponseEntity<DesignComponent> updateDesignComponent(
      @PathVariable("id") Integer id,
      @RequestBody DesignComponent request
  ) {
    DesignComponent updated = designComponentService.updateDesignComponent(id, request);
    return ResponseEntity.ok(updated);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteDesignComponent(@PathVariable("id") Integer id) {
    designComponentService.deleteDesignComponent(id);
    return ResponseEntity.noContent().build();
  }
}
