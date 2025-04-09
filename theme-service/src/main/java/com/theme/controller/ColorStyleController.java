package com.theme.controller;

import com.theme.domain.ColorStyle;
import com.theme.repository.ColorStyleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // REST API 컨트롤러
@RequestMapping("/api/color-styles") // 엔드포인트 경로 지시
public class ColorStyleController {

    private final ColorStyleRepository colorStyleRepository;

    @Autowired //ColorStyleRepository 의존성 주입
    public ColorStyleController(ColorStyleRepository colorStyleRepository) {
        this.colorStyleRepository = colorStyleRepository;
    }

    // ColorStyle 생성 API | POST /api/color-styles
    @PostMapping // HTTP POST 요청 처리
    public ResponseEntity<ColorStyle> createColorStyle(@RequestBody ColorStyle colorStyle) { //JSON -> ColorStyle 객체 변환
        ColorStyle savedColorStyle = colorStyleRepository.save(colorStyle);
        return ResponseEntity.ok(savedColorStyle);
    }

    // ColorStyle 목록 조회 API | GET /api/color-styles
    @GetMapping // HTTP GET 요청 처리
    public ResponseEntity<List<ColorStyle>> getAllColorStyles() { // 모든 ColorStyle 객체 조회하여 리스트로 반환
        List<ColorStyle> colorStyles = colorStyleRepository.findAll();
        return ResponseEntity.ok(colorStyles);
    }

    // 특정 ColorStyle 조회 API | GET /api/color-styles/{colorTypeId}
    @GetMapping("/{colorTypeId}")
    public ResponseEntity<ColorStyle> getColorStyleById(@PathVariable("colorTypeId") Integer colorTypeId) { // URL 경로에서 colorTypeId 값 추출
        return colorStyleRepository.findById(colorTypeId) // colorStyle 조회
                .map(ResponseEntity::ok) // 존재 시 해당 객체 반환
                .orElse(ResponseEntity.notFound().build()); // 없으면 404 반환
    }

    // ColorStyle 삭제 API | DELETE /api/color-styles/{colorTypeId}
    @DeleteMapping("/{colorTypeId}") // HTTP DELETE 요청 처리
    public ResponseEntity<Void> deleteColorStyle(@PathVariable("colorTypeId") Integer colorTypeId) {
        if (colorStyleRepository.existsById(colorTypeId)) {
            colorStyleRepository.deleteById(colorTypeId); //존재하면 삭제하고,
            return ResponseEntity.noContent().build(); // 204 반환
        } else {
            return ResponseEntity.notFound().build(); // 없으면 404 반환
        }
    }

    // ColorStyle 수정 API |
    @PutMapping( "/{colorTypeId}")
    public ResponseEntity<ColorStyle> updateColorStyle(@PathVariable("colorTypeId") Integer colorTypeId,@RequestBody ColorStyle colorStyle) {
        return colorStyleRepository.findById(colorTypeId).map(
                        existingColorStyle -> {
                            // 필드값만 업데이트
                            // 만약 json에 id 값을 추가하면, 바꿀 수 있도록 해야하나? id는 항상 같아야 한다고 생각...
                            existingColorStyle.setExplain(colorStyle.getExplain());
                            existingColorStyle.setIosStyleName(colorStyle.getIosStyleName());
                            existingColorStyle.setIosStyleProps(colorStyle.getIosStyleProps());
                            existingColorStyle.setAndroidStyleName(colorStyle.getAndroidStyleName());
                            // 업데이트된 엔티티 저장
                            ColorStyle updatedColorStyle = colorStyleRepository.save(existingColorStyle);
                            return ResponseEntity.ok(updatedColorStyle);
                        })
                .orElse(ResponseEntity.notFound().build());
        }
}
