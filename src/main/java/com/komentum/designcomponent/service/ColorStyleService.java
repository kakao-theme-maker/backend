package com.komentum.designcomponent.service;

import com.komentum.designcomponent.domain.ColorStyle;
import com.komentum.designcomponent.dto.ColorStyleCreateDto;
import com.komentum.designcomponent.dto.ColorStyleUpdateRequest;
import com.komentum.designcomponent.enums.StyleCode;
import com.komentum.designcomponent.mapper.ColorStyleMapper;
import com.komentum.designcomponent.repository.ColorStyleRepository;
import com.komentum.global.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
            () -> new ResourceNotFoundException("ColorStyle not found with id: " + colorStyleId));
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
   * styleCode - ColorStyle Entity 맵을 조회한다.
   * */
  @Transactional(readOnly = true)
  public Map<StyleCode, ColorStyle> findColorStyleMap() {
    return colorStyleRepository.findAll()
        .stream()
        .collect(Collectors.toMap(
            ColorStyle::getStyleCode,
            colorStyle -> colorStyle)
        );
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
