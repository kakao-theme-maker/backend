package com.komentum.seed.seeder;

import com.github.javafaker.Faker;
import com.komentum.post.consts.ThemeBoardConsts;
import com.komentum.theme.component.domain.ComponentType;
import com.komentum.theme.component.enums.Platform;
import com.komentum.theme.component.repository.ComponentTypeRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 추후 별도 PR에 구현해둔 seeder와 교체할 예정
 *
 */
@Component
@RequiredArgsConstructor
public class ComponentTypeSeeder {

  private final ComponentTypeRepository componentTypeRepository;
  private final Faker faker;

  public ComponentType createOne() {
    return ComponentType.builder()
        .platform(Platform.ANDROID)
        .explain(faker.lorem().sentence())
        .componentName(ThemeBoardConsts.DEFAULT_COMPONENT_TYPE_NAME)
        .componentPath("res/drawable-xxhdpi/theme_profile_01_image.png")
        .build();
  }

  @Transactional
  public List<ComponentType> seedData() {
    return componentTypeRepository.saveAll(List.of(createOne()));
  }
}