package com.komentum.theme.component.service;

import com.komentum.theme.component.domain.ColorStyle;
import com.komentum.theme.component.dto.ColorStyleCreateDto;
import com.komentum.theme.component.dto.ColorStyleUpdateRequest;
import com.komentum.theme.component.mapper.ColorStyleMapper;
import com.komentum.theme.component.repository.ColorStyleRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ColorStyleService {

  private final ColorStyleRepository colorStyleRepository;
  private final ColorStyleMapper colorStyleMapper;


  /**
   * color style 생성
   * @param request ColorStyle 생성을 위한 요청 DTO
   * @return 생성된 ColorStyle 반환
   * */
  @Transactional
  public ColorStyle createColorStyle(ColorStyleCreateDto request) {
    ColorStyle colorStyle = colorStyleMapper.toColorStyle(request);
    return colorStyleRepository.save(colorStyle);
  }

  /**
   * color style 단건 조회
   * @param colorStyleId ColorStyle ID
   * @return 조회된 ColorStyle 반환
   * */
  @Transactional(readOnly = true)
  public ColorStyle getColorStyleById(Integer colorStyleId) {
    return colorStyleRepository.findById(colorStyleId)
        .orElseThrow(
            () -> new EntityNotFoundException("ColorStyle not found with id: " + colorStyleId));
  }

  /**
   * color style 목록 조회
   * @return ColorStyle 목록 반환
   * */
  @Transactional(readOnly = true)
  public List<ColorStyle> getAllColorStyles() {
    return colorStyleRepository.findAll();
  }
  
  /**
   * colorStyle 갱신
   * @param colorStyleId 갱신할 colorStyle ID
   * @return 수정된 colorStyle 반환
   * */
  @Transactional
  public ColorStyle updateColorStyle(Integer colorStyleId, ColorStyleUpdateRequest request) {
    ColorStyle colorStyle = getColorStyleById(colorStyleId);
    colorStyle.update(request);
    return colorStyleRepository.save(colorStyle);
  }
}
