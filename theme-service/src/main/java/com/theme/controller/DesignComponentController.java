package com.theme.controller;

import com.theme.dto.CreateDesignComponentRequest;
import com.theme.dto.DesignComponentDto;
import com.theme.service.DesignComponentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/design-components")
public class DesignComponentController {

    private final DesignComponentService designComponentService;

    @Autowired
    public DesignComponentController(DesignComponentService designComponentService) {
        this.designComponentService = designComponentService;
    }

    @PostMapping
    public ResponseEntity<DesignComponentDto> createDesignComponent(
            @RequestBody CreateDesignComponentRequest request // 모든 데이터를 DTO로 받음
    ) {
        DesignComponentDto saved = designComponentService.createDesignComponent(request);
        return ResponseEntity.ok(saved);
    }


    @GetMapping("/{id}")
    public ResponseEntity<DesignComponentDto> getDesignComponentById(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(designComponentService.getDesignComponentById(id));
    }

    @GetMapping
    public ResponseEntity<List<DesignComponentDto>> getAllDesignComponents() {
        return ResponseEntity.ok(designComponentService.getAllDesignComponents());
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

    // componentTypeId 수정 에러
    @PutMapping("/{id}")
    public ResponseEntity<DesignComponentDto> updateDesignComponent(
            @PathVariable("id") Integer id,
            @RequestBody DesignComponentDto request,
            @RequestParam(value = "componentTypeId", required = false) Integer componentTypeId
    ) {
        DesignComponentDto updated = designComponentService.updateComponent(id, request, componentTypeId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDesignComponent(@PathVariable("id") Integer id) {
        designComponentService.deleteComponent(id);
        return ResponseEntity.ok().build();
    }
}
