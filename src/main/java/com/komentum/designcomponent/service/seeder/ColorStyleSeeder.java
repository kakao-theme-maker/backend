package com.komentum.designcomponent.service.seeder;

import com.fasterxml.jackson.databind.JsonNode;
import com.komentum.designcomponent.domain.ColorStyle;
import com.komentum.designcomponent.dto.SeedResult;
import com.komentum.designcomponent.enums.StyleCode;
import com.komentum.designcomponent.repository.ColorStyleRepository;
import com.komentum.global.utils.JsonUtils;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ColorStyleSeeder extends AbstractMapJsonSeeder<ColorStyle> {


  private final ColorStyleRepository colorStyleRepository;

  private final EntityManager entityManager;

  public static final String COLOR_STYLE_JSON_PATH = "theme-data/theme_spec_v2.json";

  public ColorStyleSeeder(JsonUtils jsonUtils, ColorStyleRepository colorStyleRepository,
      EntityManager entityManager) {
    super(jsonUtils);
    this.colorStyleRepository = colorStyleRepository;
    this.entityManager = entityManager;
  }

  @Override
  protected String getJsonPath() {
    return COLOR_STYLE_JSON_PATH;
  }

  @Override
  protected JsonNode extractFromRoot(JsonNode root) {
    return root.get("definitions").get("styleCodes");
  }

  @Override
  protected ColorStyle convertToTarget(String key, JsonNode node) {
    return ColorStyle.builder()
        .styleCode(StyleCode.from(key))
        .name(node.path("name").asText())
        .explain(node.path("description").asText())
        .build();
  }

  /**
   * color style 엔티티 목록을 기반으로 영속 상태의 ColorStyle Entity 리스트 반환
   * @return stylePropsName - ColorStyle 맵 반환
   * */
  private Map<StyleCode, ColorStyle> findPersistColorStyleMap(
      List<ColorStyle> transientSeedList) {
    List<StyleCode> identifierList = transientSeedList.stream()
        .map(ColorStyle::getStyleCode)
        .toList();
    List<ColorStyle> persistList = colorStyleRepository.findAllByStyleCodeIn(identifierList);
    return persistList.stream()
        .collect(Collectors.toMap(
            ColorStyle::getStyleCode,
            Function.identity()));
  }

  /**
   * 내부 color style seed 문서(color_style.json)를 기반으로 color style을 upsert(update/insert)한다
   * */
  @Transactional
  public SeedResult upsertColorStyleSeed() {
    int created = 0, updated = 0;
    // 부모 클래스의 readSeed(템플릿 메서드) 호출
    List<ColorStyle> transientSeedList = super.readSeed();
    Map<StyleCode, ColorStyle> persistMap = findPersistColorStyleMap(transientSeedList);
    for (ColorStyle transientSeed : transientSeedList) {
      StyleCode seedIdentifier = transientSeed.getStyleCode();
      ColorStyle persistColorStyle = persistMap.get(seedIdentifier);
      if (persistColorStyle != null && !persistColorStyle.isSame(transientSeed)) {
        updated++;
        persistColorStyle.replace(transientSeed);
      } else if (persistColorStyle == null) {
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
