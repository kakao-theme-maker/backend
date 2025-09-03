package com.komentum.theme.theme.service;

import com.komentum.global.utils.NumberUtils;
import com.komentum.theme.component.domain.ColorStyle;
import com.komentum.theme.component.domain.ComponentType;
import com.komentum.theme.component.domain.DesignComponent;
import com.komentum.theme.component.dto.CreateThemeRequest;
import com.komentum.theme.component.repository.ColorStyleRepository;
import com.komentum.theme.exception.ResourceNotFoundException;
import com.komentum.theme.theme.domain.ThemeComponent;
import com.komentum.theme.theme.domain.ThemeImage;
import com.komentum.theme.theme.domain.ThemeStyle;
import com.komentum.theme.theme.dto.ThemeComponentDto;
import com.komentum.theme.theme.dto.ThemeImageRequest;
import com.komentum.theme.theme.dto.ThemeStyleRequest;
import com.komentum.theme.theme.mapper.ThemeComponentMapper;
import com.komentum.theme.theme.mapper.ThemeImageMapper;
import com.komentum.theme.theme.mapper.ThemeStyleMapper;
import com.komentum.theme.theme.repository.ThemeComponentRepository;
import com.komentum.theme.theme.repository.ThemeImageRepository;
import com.komentum.theme.theme.repository.ThemeStyleRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ThemeManageService {

  @PersistenceContext
  private EntityManager em;

  private final ThemeComponentRepository themeComponentRepository;
  private final ThemeStyleRepository themeStyleRepository;
  private final ThemeImageRepository themeImageRepository;
  private final ColorStyleRepository colorStyleRepository;
  private final ThemeComponentMapper themeComponentMapper;
  private final ThemeStyleMapper themeStyleMapper;
  private final ThemeImageMapper themeImageMapper;

  @Transactional
  void addThemeImageAndStyle(ThemeComponent themeComponent, CreateThemeRequest request) {
    // 테마 스타일 추가
    if (request.getStyles() != null) {
      List<Integer> colorStyleIdList = request.getStyles().stream()
          .map(ThemeStyleRequest::getColorTypeId).toList();
      Map<Integer, ColorStyle> colorstyleMap = colorStyleRepository.findAllById(colorStyleIdList)
          .stream().collect(
              Collectors.toMap(ColorStyle::getColorStyleId, Function.identity()));
      request.getStyles().forEach(createDto -> {
        ColorStyle colorStyle = colorstyleMap.get(createDto.getColorTypeId());
        ThemeStyle themeStyle = themeStyleMapper.convertToTransientEntity(createDto, colorStyle);
        themeComponent.addThemeStyle(themeStyle);
      });
    }
    // 테마 이미지 추가
    if (request.getImages() != null) {
      for (ThemeImageRequest imageRequest : request.getImages()) {
        ComponentType proxyComponentType = em.getReference(ComponentType.class,
            imageRequest.getComponentTypeId());
        DesignComponent proxyDesignComponent = em.getReference(DesignComponent.class,
            imageRequest.getDesignComponentId());
        ThemeImage themeImage = themeImageMapper.convertToTransientEntity(proxyComponentType,
            proxyDesignComponent);
        themeComponent.addThemeImage(themeImage);
      }
    }
  }

  @Transactional
  public ThemeComponentDto createTheme(CreateThemeRequest request) {
    // 테마 컴포넌트 생성
    ThemeComponent themeComponent = themeComponentMapper.convertToTransientEntity(request, "1");
    // 테마 스타일 및 이미지 추가
    addThemeImageAndStyle(themeComponent, request);
    // 테마 컴포넌트 저장
    return themeComponentMapper.convertToDto(themeComponentRepository.save(themeComponent));
  }

  @Transactional
  public ThemeComponentDto updateTheme(Integer id, CreateThemeRequest request) {
    // 저장된 테마 조회
    ThemeComponent themeComponent = themeComponentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Theme not found with id: " + id));
    // 기본 정보 업데이트
    themeComponent.setThemeName(request.getThemeName());
    themeComponent.setVersionName(request.getVersionName());
    if (request.getIsPublic() != null) {
      themeComponent.setIsPublic(request.getIsPublic());
    }
    if (NumberUtils.isNumericString(themeComponent.getVersionNumber())) {
      themeComponent.setVersionNumber(themeComponent.getVersionNumber() + 1);
    } else {
      throw new RuntimeException("Version number is not numeric");
    }
    // 테마 스타일과 이미지 초기화 후 갱신
    themeComponent.getThemeImages().clear();
    themeComponent.getThemeStyles().clear();
    addThemeImageAndStyle(themeComponent, request);
    // 결과 반환
    return themeComponentMapper.convertToDto(themeComponentRepository.save(themeComponent));
  }

  @Transactional
  public void deleteTheme(Integer id) {
    ThemeComponent themeComponent = themeComponentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Theme not found with id: " + id));
    // 먼저 관련된 테마 스타일과 테마 이미지를 삭제
    themeStyleRepository.deleteByThemeComponentId(id);
    themeImageRepository.deleteByThemeComponentId(id);
    // 그 다음 테마 컴포넌트를 삭제
    themeComponentRepository.delete(themeComponent);
  }

  @Transactional
  public ThemeComponentDto markAsDone(Integer id) {
    ThemeComponent themeComponent = themeComponentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Theme not found with id: " + id));
    themeComponent.setIsDone(true);
    themeComponentRepository.save(themeComponent);
    return themeComponentMapper.convertToDto(themeComponent);
  }
}
