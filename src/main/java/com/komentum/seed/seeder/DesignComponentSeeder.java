package com.komentum.seed.seeder;

import com.github.javafaker.Faker;
import com.komentum.designcomponent.domain.ComponentType;
import com.komentum.global.utils.FileManager;
import com.komentum.designcomponent.domain.DesignComponent;
import com.komentum.designcomponent.repository.ComponentTypeRepository;
import com.komentum.designcomponent.repository.DesignComponentRepository;
import com.komentum.user.domain.User;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DesignComponentSeeder {

  private final FileManager fileManager;
  private final DesignComponentRepository designComponentRepository;
  private final ComponentTypeRepository componentTypeRepository;
  private final Faker faker;

  private DesignComponent generateOne(User owner, ComponentType componentType) {
    try {
      ClassPathResource targetImage = new ClassPathResource("dev_resource/test.png");
      String fileName =
          "DesignComponent_" + UUID.randomUUID() + "_" + System.currentTimeMillis() + ".png";
      try (InputStream is = targetImage.getInputStream()) {
        fileManager.uploadFile(is.readAllBytes(), fileName);
      }
      String fileUrl = fileManager.resolveFilePath(fileName);
      DesignComponent designComponent = DesignComponent.builder()
          .user(owner)
          .imageUrl(fileUrl)
          .isPublic(true)
          .build();
      if (componentType != null) {
        designComponent.replaceComponentTypes(List.of(componentType));
      }
      return designComponent;
    } catch (IOException e) {
      log.warn("failed to load sample image");
      throw new RuntimeException("[DesignComponetSeeder] failed to load sample image");
    }
  }

  @Transactional
  public List<DesignComponent> seedPeruser(int size, List<User> owners) {
    List<String> userEmails = owners.stream().map(User::getUserEmail).toList();
    List<DesignComponent> existing = designComponentRepository.findByUser_UserEmailIn(userEmails);
    if (existing.size() >= size * owners.size()) {
      return existing;
    }
    List<DesignComponent> designComponents = new ArrayList<>();
    List<ComponentType> componentTypes = componentTypeRepository.findAll();
    int componentTypeIndex = 0;
    for (User owner : owners) {
      for (int i = 0; i < size; i++) {
        ComponentType componentType = componentTypes.isEmpty() ?
            null :
            componentTypes.get(componentTypeIndex++ % componentTypes.size());
        designComponents.add(generateOne(owner, componentType));
      }
    }
    return designComponentRepository.saveAll(designComponents);
  }

}
