package com.komentum.theme.component.service.seeder;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Functions;
import com.komentum.theme.component.domain.ColorStyle;
import com.komentum.theme.component.domain.PlatformColorStyle;
import com.komentum.theme.component.dto.SeedResult;
import com.komentum.theme.component.enums.Platform;
import com.komentum.theme.component.enums.StyleCode;
import com.komentum.theme.component.repository.ColorStyleRepository;
import com.komentum.theme.component.repository.PlatformColorStyleRepository;
import com.komentum.theme.utils.JsonUtils;
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
public class PlatformColorStyleSeeder {

  private final JsonUtils jsonUtils;
  private final PlatformColorStyleRepository platformColorStyleRepository;
  private final ColorStyleRepository colorStyleRepository;

  public static final String SEED_FILE_PATH = "theme-data/theme_spec_v2.json";

  /**
   * seed를 플랫폼별로 읽는다
   * @param root SEED_FILE_PATH를 통해 읽은 json 파일 데이터
   * @param platform 대상 플랫폼
   * @param colorStyleMap styleCode : colorStyle 맵
   * @return platformColorStyle에 대한 시드 목록
   * */
  private List<PlatformColorStyle> readSeedByPlatform(JsonNode root, Platform platform,
      Map<StyleCode, ColorStyle> colorStyleMap) {
    JsonNode platformStyleCodeNodes = root.path("definitions")
        .path("platformMappings")
        .path(platform.getPlatformName())
        .path("styleCodes");
    if (jsonUtils.isInvalidNode(platformStyleCodeNodes)) {
      throw new IllegalStateException(
          "Invalid JSON Structure : " + this.getClass().getSimpleName());
    }
    List<PlatformColorStyle> result = new ArrayList<>();
    Iterator<Entry<String, JsonNode>> fields = platformStyleCodeNodes.fields();
    while (fields.hasNext()) {
      Entry<String, JsonNode> platformColorStyle = fields.next();
      StyleCode styleCode = StyleCode.from(platformColorStyle.getKey());
      ColorStyle colorStyle = Optional.ofNullable(colorStyleMap.get(styleCode))
          .orElseThrow(() -> new IllegalStateException((
              "Invalid color style with style code : " + styleCode.getStyleCode())));
      for (JsonNode node : platformColorStyle.getValue()) {
        result.add(
            PlatformColorStyle.builder()
                .platform(platform)
                .resourceGroup(node.path("resourceGroup").asText())
                .resourceName(node.path("resourceName").asText())
                .code(node.path("code").asText())
                .colorStyle(colorStyle)
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
  private SeedResult persistTransientSeeds(List<PlatformColorStyle> transientSeeds) {
    int updated = 0, created = 0;
    // 영속 상태 엔티티 조회
    Map<String, PlatformColorStyle> persistEntityMap = platformColorStyleRepository.findAll()
        .stream()
        .collect(Collectors.toMap(
            PlatformColorStyle::getCode,
            Functions.identity()
        ));
    // 영속 데이터 유무에 따라 데이터 생성 혹은 갱신
    for (PlatformColorStyle transientSeed : transientSeeds) {
      String uniqueSeedCode = transientSeed.getCode();
      PlatformColorStyle persistEntity = persistEntityMap.get(uniqueSeedCode);
      if (persistEntity == null) { // DB에 없다면 저장
        platformColorStyleRepository.save(transientSeed);
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

  @Transactional
  public SeedResult upsertPlatformColorStyle() {
    List<Platform> targetPlatforms = List.of(Platform.ANDROID, Platform.IOS);
    try {
      JsonNode root = jsonUtils.readJsonNode(SEED_FILE_PATH);
      Map<StyleCode, ColorStyle> colorStyleMap = colorStyleRepository.findAll()
          .stream()
          .collect(Collectors.toMap(
              ColorStyle::getStyleCode,
              Functions.identity()
          ));
      List<PlatformColorStyle> transientSeeds = new ArrayList<>();
      for (Platform platform : targetPlatforms) {
        transientSeeds.addAll(readSeedByPlatform(root, platform, colorStyleMap));
      }
      return persistTransientSeeds(transientSeeds);
    } catch (Exception e) {
      log.error("failed to seed platform color style : {}", SEED_FILE_PATH, e);
      throw new RuntimeException("failed to seed platform color style : " + SEED_FILE_PATH, e);
    }
  }
}
