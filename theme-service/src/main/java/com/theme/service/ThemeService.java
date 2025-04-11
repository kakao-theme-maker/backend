package com.theme.service;

import com.theme.domain.*;
import com.theme.dto.*;
import com.theme.exception.ResourceNotFoundException;
import com.theme.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ThemeService {

    private final ThemeComponentRepository themeComponentRepository;
    private final ColorStyleRepository colorStyleRepository;
    private final ThemeStyleRepository themeStyleRepository;
    private final ThemeImageRepository themeImageRepository;
    private final DesignComponentRepository designComponentRepository;
    private final ComponentTypeRepository componentTypeRepository;
    @Autowired
    public ThemeService(ThemeComponentRepository themeComponentRepository,
                        ColorStyleRepository colorStyleRepository,
                        ThemeStyleRepository themeStyleRepository,
                        ThemeImageRepository themeImageRepository,
                        DesignComponentRepository designComponentRepository,
                        ComponentTypeRepository componentTypeRepository) {
        this.themeComponentRepository = themeComponentRepository;
        this.colorStyleRepository = colorStyleRepository;
        this.themeStyleRepository = themeStyleRepository;
        this.themeImageRepository = themeImageRepository;
        this.designComponentRepository = designComponentRepository;
        this.componentTypeRepository = componentTypeRepository;
    }

    @Transactional(readOnly = true)
    public List<ThemeComponentDto> getAllThemes() {
        return themeComponentRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ThemeComponentDto> getThemesByUserEmail(String userEmail) {
        return themeComponentRepository.findByUserEmail(userEmail).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ThemeComponentDto> getPublicThemes() {
        return themeComponentRepository.findByIsPublicTrue().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ThemeComponentDto getThemeById(Integer id) {
        ThemeComponent themeComponent = themeComponentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theme not found with id: " + id));
        return convertToDto(themeComponent);
    }

    @Transactional
    public ThemeComponentDto createTheme(CreateThemeRequest request) {
        // 새 버전 번호 계산 (해당 사용자의 같은 테마 이름에 대해 최신 버전 + 1)
        int newVersionNumber = 1;
        List<ThemeComponent> existingThemes = themeComponentRepository.findByUserEmail(request.getUserEmail()).stream()
                .filter(t -> t.getThemeName().equals(request.getThemeName()))
                .collect(Collectors.toList());

        if (!existingThemes.isEmpty()) {
            newVersionNumber = existingThemes.stream()
                    .mapToInt(t -> Integer.parseInt(t.getVersionNumber()))
                    .max()
                    .orElse(0) + 1;
        }

        // 테마 컴포넌트 생성
        ThemeComponent themeComponent = ThemeComponent.builder()
                .userEmail(request.getUserEmail())
                .themeName(request.getThemeName())
                .versionNumber(String.valueOf(newVersionNumber))
                .versionName(request.getVersionName())
                .isDone(false)
                .isPublic(request.getIsPublic() != null ? request.getIsPublic() : false)
                .build();

        themeComponent = themeComponentRepository.save(themeComponent);

        // 테마 스타일 추가
        if (request.getStyles() != null) {
            for (ThemeStyleRequest styleRequest : request.getStyles()) {
                ColorStyle colorStyle = colorStyleRepository.findById(styleRequest.getColorTypeId())
                        .orElseThrow(() -> new ResourceNotFoundException("ColorStyle not found with id: " + styleRequest.getColorTypeId()));

                ThemeStyle themeStyle = ThemeStyle.builder()
                        .themeComponentId(themeComponent.getThemeComponentId())
                        .colorTypeId(colorStyle.getColorTypeId())
                        .themeComponent(themeComponent)
                        .colorStyle(colorStyle)
                        .color(styleRequest.getColor())
                        .build();

                themeStyleRepository.save(themeStyle);
            }
        }

        // 테마 이미지 추가
        if (request.getDesignComponentIds() != null) {
            for (Integer designComponentId : request.getDesignComponentIds()) {
                // ThemeImage 객체 생성 시 ThemeImageId를 직접 생성하여 설정
                ThemeImageId imageId = new ThemeImageId(themeComponent.getThemeComponentId(), designComponentId);

                ThemeImage themeImage = new ThemeImage();
                themeImage.setId(imageId);
                themeImage.setThemeComponent(themeComponent);

                themeImageRepository.save(themeImage);
            }
        }

        return getThemeById(themeComponent.getThemeComponentId());
    }

    @Transactional
    public ThemeComponentDto updateTheme(Integer id, CreateThemeRequest request) {
        ThemeComponent themeComponent = themeComponentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theme not found with id: " + id));

        // 기본 정보 업데이트
        themeComponent.setThemeName(request.getThemeName());
        themeComponent.setVersionName(request.getVersionName());
        if (request.getIsPublic() != null) {
            themeComponent.setIsPublic(request.getIsPublic());
        }

        themeComponentRepository.save(themeComponent);

        // 기존 스타일 삭제
        themeStyleRepository.deleteByThemeComponentId(id);

        // 새 스타일 추가
        if (request.getStyles() != null) {
            for (ThemeStyleRequest styleRequest : request.getStyles()) {
                ColorStyle colorStyle = colorStyleRepository.findById(styleRequest.getColorTypeId())
                        .orElseThrow(() -> new ResourceNotFoundException("ColorStyle not found with id: " + styleRequest.getColorTypeId()));

                ThemeStyle themeStyle = ThemeStyle.builder()
                        .themeComponentId(themeComponent.getThemeComponentId())
                        .colorTypeId(colorStyle.getColorTypeId())
                        .themeComponent(themeComponent)
                        .colorStyle(colorStyle)
                        .color(styleRequest.getColor())
                        .build();

                themeStyleRepository.save(themeStyle);
            }
        }

        // 기존 이미지 삭제
        themeImageRepository.deleteByThemeComponentId(id);

        // 새 이미지 추가
        if (request.getDesignComponentIds() != null) {
            for (Integer designComponentId : request.getDesignComponentIds()) {
                // ThemeImage 객체 생성 시 ThemeImageId를 직접 생성하여 설정
                ThemeImageId imageId = new ThemeImageId(themeComponent.getThemeComponentId(), designComponentId);

                ThemeImage themeImage = new ThemeImage();
                themeImage.setId(imageId);
                themeImage.setThemeComponent(themeComponent);

                themeImageRepository.save(themeImage);
            }
        }

        return getThemeById(themeComponent.getThemeComponentId());
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


    @Transactional(readOnly = true)
    public List<ThemeComponentDto> getCompletedThemes() {
        List<ThemeComponent> completedThemes = themeComponentRepository.findByIsDoneTrue();
        return completedThemes.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<ThemeComponentDto> getCompletedThemesByUser(String userEmail) {
        List<ThemeComponent> completedThemes = themeComponentRepository.findByUserEmailAndIsDoneTrue(userEmail);
        return completedThemes.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }



    @Transactional
    public ThemeComponentDto markAsDone(Integer id) {
        ThemeComponent themeComponent = themeComponentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theme not found with id: " + id));

        themeComponent.setIsDone(true);
        themeComponentRepository.save(themeComponent);

        return convertToDto(themeComponent);
    }

    private ThemeComponentDto convertToDto(ThemeComponent themeComponent) {
        List<ThemeStyle> styles = themeStyleRepository.findByThemeComponentId(themeComponent.getThemeComponentId());
        List<ThemeImage> images = themeImageRepository.findByThemeComponentId(themeComponent.getThemeComponentId());

        List<ThemeStyleDto> styleDtos = styles.stream()
                .map(style -> ThemeStyleDto.builder()
                        .colorTypeId(style.getColorStyle().getColorTypeId())
                        .explain(style.getColorStyle().getExplain())
                        .iosStyleName(style.getColorStyle().getIosStyleName())
                        .androidStyleName(style.getColorStyle().getAndroidStyleName())
                        .color(style.getColor())
                        .build())
                .collect(Collectors.toList());

        List<ThemeImageDto> imageDtos = images.stream()
                .map(image -> {
                    // ThemeImage에서 designComponentId를 가져오는 방법 수정
                    Integer designComponentId = image.getId().getDesignComponentId();
                    return ThemeImageDto.builder()
                            .designComponentId(designComponentId)
                            .build();
                })
                .collect(Collectors.toList());

        return ThemeComponentDto.builder()
                .themeComponentId(themeComponent.getThemeComponentId())
                .userEmail(themeComponent.getUserEmail())
                .themeName(themeComponent.getThemeName())
                .versionNumber(themeComponent.getVersionNumber())
                .versionName(themeComponent.getVersionName())
                .isDone(themeComponent.getIsDone())
                .isPublic(themeComponent.getIsPublic())
                .styles(styleDtos)
                .images(imageDtos)
                .build();
    }
}
