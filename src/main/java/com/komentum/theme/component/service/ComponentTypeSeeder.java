package com.komentum.theme.component.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.komentum.theme.component.domain.ComponentType;
import com.komentum.theme.component.dto.SeedResult;
import com.komentum.theme.component.enums.TypeCode;
import com.komentum.theme.component.repository.ComponentTypeRepository;
import com.komentum.theme.utils.JsonUtils;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ComponentTypeSeeder extends AbstractMapJsonSeeder<ComponentType> {

  private final ComponentTypeRepository componentTypeRepository;

  private final EntityManager entityManager;

  public static final String COMPONENT_TYPE_JSON_PATH = "theme-data/theme_spec.json";

  public ComponentTypeSeeder(JsonUtils jsonUtils, ComponentTypeRepository componentTypeRepository,
      EntityManager entityManager) {
    super(jsonUtils);
    this.componentTypeRepository = componentTypeRepository;
    this.entityManager = entityManager;
  }

  @Override
  protected String getJsonPath() {
    return COMPONENT_TYPE_JSON_PATH;
  }

  @Override
  protected JsonNode extractFromRoot(JsonNode root) {
    return root.get("definitions").get("typeCodes");
  }

  @Override
  protected ComponentType convertToTarget(String key, JsonNode node) {
    return ComponentType.builder()
        .typeCode(TypeCode.from(key))
        .name(node.path("name").asText())
        .explain(node.path("description").asText())
        .build();
  }

  /**
   * seed 데이터 기반으로 DB에 존재하는 영속 상태의 ComponentType을 조회한다
   * @param transientSeedList seed 기반 비영속 ComponentType
   * @return 엉속 상태의 ComponentType 목록
   * */
  private Map<TypeCode, ComponentType> findPersistComponentTypeMap(
      List<ComponentType> transientSeedList) {
    List<TypeCode> identifierList = transientSeedList.stream()
        .map(ComponentType::getTypeCode)
        .toList();
    List<ComponentType> persistList = componentTypeRepository.findAllByTypeCodeIn(identifierList);
    return persistList.stream()
        .collect(Collectors.toMap(
            ComponentType::getTypeCode,
            Function.identity()));
  }

  /**
   * seed를 기반으로 DB에 ComponentType을 생성 및 갱신한다
   * */
  @Transactional
  public SeedResult upsertComponentType() {
    int created = 0, updated = 0;
    List<ComponentType> transientSeedList = super.readSeed();
    Map<TypeCode, ComponentType> persistMap = findPersistComponentTypeMap(transientSeedList);
    for (ComponentType transientSeed : transientSeedList) {
      TypeCode seedIdentifier = transientSeed.getTypeCode();
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
