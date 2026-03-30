package com.komentum.theme.component.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.komentum.theme.component.domain.ComponentType;
import com.komentum.theme.component.dto.SeedResult;
import com.komentum.theme.component.enums.Platform;
import com.komentum.theme.component.repository.ComponentTypeRepository;
import com.komentum.theme.utils.JsonUtils;
import jakarta.persistence.EntityManager;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ComponentTypeSeeder {

  private final JsonUtils jsonUtils;

  private final ComponentTypeRepository componentTypeRepository;

  private final EntityManager entityManager;

  public static final String COMPONENT_TYPE_JSON_PATH = "theme-data/component_type.json";

  public static class ComponentTypeSeed {

    @JsonProperty("platform")
    Platform platform;
    @JsonProperty("component_path")
    String componentPath;
    @JsonProperty("component_name")
    String componentName;

    public ComponentType toEntity() {
      return ComponentType.builder()
          .platform(platform)
          .componentPath(componentPath)
          .componentName(componentName)
          .build();
    }
  }

  /**
   * component type의 seed 문서를 읽어서 ComponentType 목록 반환
   * @return 비영속 상태의 seed 기반 component type 목록
   * */
  private List<ComponentType> readComponentTypeSeedList() {
    try {
      return jsonUtils.readListAsType(COMPONENT_TYPE_JSON_PATH, ComponentTypeSeed.class)
          .stream().map(ComponentTypeSeed::toEntity)
          .toList();
    } catch (IOException e) {
      throw new RuntimeException(
          "Failed to read component type seed file: " + COMPONENT_TYPE_JSON_PATH,
          e);
    }
  }

  /**
   * seed 데이터 기반으로 DB에 존재하는 영속 상태의 ComponentType을 조회한다
   * @param transientSeedList seed 기반 비영속 ComponentType
   * @return 엉속 상태의 ComponentType 목록
   * */
  private Map<String, ComponentType> findPersistComponentTypeMap(
      List<ComponentType> transientSeedList) {
    List<String> identifierList = transientSeedList.stream()
        .map(ComponentType::getComponentPath)
        .toList();
    List<ComponentType> persistList = componentTypeRepository.findAllByComponentPathIn(
        identifierList);
    return persistList.stream()
        .collect(Collectors.toMap(
            ComponentType::getComponentPath,
            Function.identity()));
  }

  /**
   * seed를 기반으로 DB에 ComponentType을 생성 및 갱신한다
   * */
  @Transactional
  public SeedResult upsertComponentType() {
    int created = 0, updated = 0;
    List<ComponentType> transientSeedList = readComponentTypeSeedList();
    Map<String, ComponentType> persistMap = findPersistComponentTypeMap(transientSeedList);
    for (ComponentType transientSeed : transientSeedList) {
      String seedIdentifier = transientSeed.getComponentPath();
      ComponentType persistComponentType = persistMap.get(seedIdentifier);
      if (persistComponentType != null && !persistComponentType.isSame(transientSeed)) {
        updated++;
        persistComponentType.replace(transientSeed);
      } else if (persistComponentType == null) {
        created++;
        entityManager.persist(transientSeed);
      }
    }
    return SeedResult.builder()
        .created(created)
        .updated(updated)
        .build();
  }
}
