package com.komentum.designcomponent.service.seeder;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Functions;
import com.komentum.designcomponent.domain.ComponentType;
import com.komentum.designcomponent.domain.PlatformComponentType;
import com.komentum.designcomponent.dto.SeedResult;
import com.komentum.designcomponent.enums.Platform;
import com.komentum.designcomponent.enums.TypeCode;
import com.komentum.designcomponent.repository.ComponentTypeRepository;
import com.komentum.designcomponent.repository.PlatformComponentTypeRepository;
import com.komentum.global.enums.FileExtension;
import com.komentum.global.utils.JsonUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlatformComponentTypeSeeder {

  private final JsonUtils jsonUtils;
  private final ComponentTypeRepository componentTypeRepository;
  private final PlatformComponentTypeRepository platformComponentTypeRepository;


  public static final String SEED_FILE_PATH = "theme-data/theme_spec_v2.json";

  /**
   * seed를 플랫폼별로 읽는다
   * @param root SEED_FILE_PATH를 통해 읽은 json 파일 데이터
   * @param platform 대상 플랫폼
   * @param componentTypeMap typeCode : componentType 맵
   * @return platformComponentType에 대한 시드 목록
   * */
  private List<PlatformComponentType> readSeedByPlatform(JsonNode root, Platform platform,
      Map<TypeCode, ComponentType> componentTypeMap) {
    JsonNode platformTypeCodeNodes = root.path("definitions")
        .path("platformMappings")
        .path(platform.getPlatformName())
        .path("typeCodes");
    if (jsonUtils.isInvalidNode(platformTypeCodeNodes)) {
      throw new IllegalStateException(
          "Invalid JSON Structure : " + this.getClass().getSimpleName());
    }
    List<PlatformComponentType> result = new ArrayList<>();
    Iterator<Entry<String, JsonNode>> fields = platformTypeCodeNodes.fields();
    while (fields.hasNext()) {
      Entry<String, JsonNode> platformComponentType = fields.next();
      TypeCode typeCode = TypeCode.from(platformComponentType.getKey());
      ComponentType componentType = Optional.ofNullable(componentTypeMap.get(typeCode))
          .orElseThrow(() -> new IllegalStateException((
              "Invalid component type with type code : " + typeCode.getTypeCode())));
      for (JsonNode node : platformComponentType.getValue()) {
        String fileExtension = node.path("fileExtension").asText();
        result.add(
            PlatformComponentType.builder()
                .platform(platform)
                .path(node.path("path").asText())
                .width(node.path("width").asInt())
                .height(node.path("height").asInt())
                .fileExtension(FileExtension.from(fileExtension))
                .code(node.path("code").asText())
                .componentType(componentType)
                .build()
        );
      }
    }
    return result;
  }

  /**
   * <p>seed를 기반으로 데이터를 갱신하고, 그 결과를 반환한다</p>
   * <p>이 메서드는 성능과 정상적인 동작을 위해 transaction 내에서 실행되어야 한다</p>
   * @param transientSeeds 비영속 상태의 seed 데이터
   * @return 데이터 갱신 / 생성 횟수
   * */
  private SeedResult persistTransientSeeds(List<PlatformComponentType> transientSeeds) {
    int updated = 0, created = 0;
    // 영속 상태 엔티티 조회
    Map<String, PlatformComponentType> persistEntityMap = platformComponentTypeRepository.findAll()
        .stream()
        .collect(Collectors.toMap(
            PlatformComponentType::getCode,
            Functions.identity()
        ));
    // 영속 데이터 유무에 따라 데이터 생성 혹은 갱신
    for (PlatformComponentType transientSeed : transientSeeds) {
      String uniqueSeedCode = transientSeed.getCode();
      PlatformComponentType persistEntity = persistEntityMap.get(uniqueSeedCode);
      if (persistEntity == null) { // DB에 없다면 저장
        platformComponentTypeRepository.save(transientSeed);
        created++;
      } else { // DB에 있다면 갱신
        persistEntity.replace(transientSeed);
        updated++;
      }
    }
    return SeedResult.builder()
        .created(created)
        .updated(updated)
        .build();
  }

  // theme_spec_v2.json에 각 platformComponentType에 code 추가하기
  @Transactional
  public SeedResult upsertPlatformComponentType() {
    List<Platform> targetPlatforms = List.of(Platform.ANDROID, Platform.IOS);
    try {
      JsonNode root = jsonUtils.readJsonNode(SEED_FILE_PATH);
      Map<TypeCode, ComponentType> componentTypeMap = componentTypeRepository.findAll()
          .stream()
          .collect(Collectors.toMap(
              ComponentType::getTypeCode,
              Functions.identity()
          ));
      List<PlatformComponentType> transientSeeds = new ArrayList<>();
      for (Platform platform : targetPlatforms) {
        transientSeeds.addAll(readSeedByPlatform(root, platform, componentTypeMap));
      }
      return persistTransientSeeds(transientSeeds);
    } catch (Exception e) {
      log.error("failed to seed platform component type : {}", SEED_FILE_PATH, e);
      throw new RuntimeException("failed to seed platform component type : " + SEED_FILE_PATH, e);
    }
  }
}
