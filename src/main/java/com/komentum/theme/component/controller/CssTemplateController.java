package com.komentum.theme.component.controller;

import com.komentum.theme.component.domain.ColorStyle;
import com.komentum.theme.component.service.ColorStyleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/css-options")
@RequiredArgsConstructor
@Tag(name = "CSS Options", description = "CSS 커스터마이징 옵션 관리 API")
public class CssTemplateController {

    private final ColorStyleService colorStyleService;

    @GetMapping
    @Operation(summary = "모든 CSS 옵션 조회", description = "사용 가능한 모든 CSS 커스터마이징 옵션을 조회합니다.")
    public ResponseEntity<List<ColorStyle>> getAllCssOptions() {
        List<ColorStyle> options = colorStyleService.getAllCssOptions();
        return ResponseEntity.ok(options);
    }

    @GetMapping("/categories")
    @Operation(summary = "CSS 옵션 카테고리 조회", description = "사용 가능한 모든 CSS 옵션 카테고리를 조회합니다.")
    public ResponseEntity<Set<String>> getCategories() {
        Map<String, List<ColorStyle>> categorized = colorStyleService.getCssOptionsByCategory();
        return ResponseEntity.ok(categorized.keySet());
    }

    @GetMapping("/categories/{category}")
    @Operation(summary = "카테고리별 CSS 옵션 조회", description = "특정 카테고리의 CSS 옵션을 조회합니다.")
    public ResponseEntity<List<ColorStyle>> getOptionsByCategory(
            @Parameter(description = "CSS 옵션 카테고리") @PathVariable String category) {
        
        List<ColorStyle> options = colorStyleService.getCssOptionsByCategory(category);
        return ResponseEntity.ok(options);
    }

    @GetMapping("/grouped-by-category")
    @Operation(summary = "카테고리별 그룹화된 CSS 옵션 조회", description = "모든 CSS 옵션을 카테고리별로 그룹화하여 조회합니다.")
    public ResponseEntity<Map<String, List<ColorStyle>>> getOptionsGroupedByCategory() {
        Map<String, List<ColorStyle>> groupedOptions = colorStyleService.getCssOptionsByCategory();
        return ResponseEntity.ok(groupedOptions);
    }

    @GetMapping("/{colorTypeId}")
    @Operation(summary = "CSS 옵션 상세 조회", description = "특정 CSS 옵션의 상세 정보를 조회합니다.")
    public ResponseEntity<ColorStyle> getCssOptionById(
            @Parameter(description = "색상 타입 ID") @PathVariable Integer colorTypeId) {
        
        ColorStyle option = colorStyleService.getCssOptionById(colorTypeId);
        return ResponseEntity.ok(option);
    }

    @PostMapping("/initialize")
    @Operation(summary = "CSS 옵션 데이터 초기화", description = "exampleCss.css 기반으로 CSS 옵션 데이터를 초기화합니다.")
    public ResponseEntity<String> initializeCssOptions() {
        try {
            colorStyleService.initializeCssOptions();
            return ResponseEntity.ok("CSS 옵션 데이터가 성공적으로 초기화되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("CSS 옵션 데이터 초기화 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}