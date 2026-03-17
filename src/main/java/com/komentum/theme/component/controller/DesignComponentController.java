package com.komentum.theme.component.controller;

import com.komentum.theme.component.dto.CreateDesignComponentRequest;
import com.komentum.theme.component.dto.DesignComponentDto;
import com.komentum.theme.component.dto.UpdateDesignComponentRequest;
import com.komentum.theme.component.service.DesignComponentService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
@RequestMapping("/api/design-components")
@RequiredArgsConstructor
public class DesignComponentController {

  private final DesignComponentService designComponentService;

  @PostMapping
  public ResponseEntity<DesignComponentDto> createDesignComponent(
      @Valid @RequestBody CreateDesignComponentRequest request
  ) {
    DesignComponentDto saved = designComponentService.createDesignComponent(request);
    return ResponseEntity.ok(saved);
  }

  @GetMapping("/{id}")
  public ResponseEntity<DesignComponentDto> getDesignComponentById(@PathVariable("id") Integer id) {
    return ResponseEntity.ok(designComponentService.getDesignComponentById(id));
  }

  @GetMapping
  public ResponseEntity<Page<DesignComponentDto>> getAllDesignComponents(
      @PageableDefault(size = 20, sort = "createdAt") @ParameterObject Pageable pageable) {
    return ResponseEntity.ok(designComponentService.getAllDesignComponents(pageable));
  }

  @GetMapping("/user/{userEmail}")
  public ResponseEntity<List<DesignComponentDto>> getDesignComponentsByUserEmail(
      @PathVariable("userEmail") String userEmail
  ) {
    return ResponseEntity.ok(designComponentService.getByUserEmail(userEmail));
  }

  @GetMapping("/public")
  public ResponseEntity<List<DesignComponentDto>> getPublicDesignComponents() {
    return ResponseEntity.ok(designComponentService.getPublicComponents());
  }

  @PutMapping("/{id}")
  public ResponseEntity<DesignComponentDto> updateDesignComponent(
      @PathVariable("id") Integer id,
      @Valid @RequestBody UpdateDesignComponentRequest request
  ) {
    DesignComponentDto updated = designComponentService.updateDesignComponent(id, request);
    return ResponseEntity.ok(updated);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteDesignComponent(@PathVariable("id") Integer id) {
    designComponentService.deleteComponent(id);
    return ResponseEntity.noContent().build();
  }
}