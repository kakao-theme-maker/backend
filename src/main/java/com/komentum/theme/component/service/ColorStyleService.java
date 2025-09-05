package com.komentum.theme.component.service;

import com.komentum.theme.component.domain.ColorStyle;
import com.komentum.theme.component.repository.ColorStyleRepository;
import com.komentum.theme.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ColorStyleService {

  private final ColorStyleRepository colorStyleRepository;

  public ColorStyle createColorStyle(ColorStyle colorStyle) {
    return colorStyleRepository.save(colorStyle);
  }

  @Transactional(readOnly = true)
  public ColorStyle getColorStyleById(Integer id) {
    return colorStyleRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("ColorStyle not found with id: " + id));
  }

  @Transactional(readOnly = true)
  public List<ColorStyle> getAllColorStyles() {
    return colorStyleRepository.findAll();
  }

  public ColorStyle updateColorStyle(Integer id, ColorStyle colorStyleDetails) {
    ColorStyle colorStyle = getColorStyleById(id);

    Optional.ofNullable(colorStyleDetails.getExplain()).ifPresent(colorStyle::setExplain);
    Optional.ofNullable(colorStyleDetails.getIosStyleName()).ifPresent(colorStyle::setIosStyleName);
    Optional.ofNullable(colorStyleDetails.getAndroidStyleName()).ifPresent(colorStyle::setAndroidStyleName);

    return colorStyleRepository.save(colorStyle);
  }

  public void deleteColorStyle(Integer id) {
    if (!colorStyleRepository.existsById(id)) {
      throw new ResourceNotFoundException("ColorStyle not found with id: " + id);
    }
    colorStyleRepository.deleteById(id);
  }
}
