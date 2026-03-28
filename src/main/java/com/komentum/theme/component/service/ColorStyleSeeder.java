package com.komentum.theme.component.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.komentum.theme.component.domain.ColorStyle;
import com.komentum.theme.component.dto.SeedResult;
import com.komentum.theme.component.enums.Platform;
import com.komentum.theme.component.repository.ColorStyleRepository;
import com.komentum.theme.utils.JsonUtils;
import jakarta.persistence.EntityManager;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ColorStyleSeeder {

  private final JsonUtils jsonUtils;

  private final ColorStyleRepository colorStyleRepository;

  private final EntityManager entityManager;

  public static final String COLOR_STYLE_JSON_PATH = "theme-data/color_style.json";

  @Getter
  public static class ColorStyleSeed {

    String explain;

    String platform;

    @JsonProperty("sheet_style_path")
    String sheetStylePath;

    @JsonProperty("sheet_element_name")
    String sheetElementName;

    @JsonProperty("sheet_props_name")
    String sheetPropsName;

    public ColorStyle toEntity() {
      return ColorStyle.builder()
          .explain(explain)
          .stylePropsName(sheetPropsName)
          .styleElementName(sheetElementName)
          .styleSheetPath(sheetStylePath)
          .platform(Platform.fromString(platform))
          .build();
    }
  }

  /**
   * 내부 color style seed 문서(color_style.json)에서 시드 읽기
   * @return 비영속 상태의 시드 ColorStyle 반환
   * */
  private List<ColorStyle> readColorStyleSeedList() {
    try {
      return jsonUtils.readListAsType(COLOR_STYLE_JSON_PATH, ColorStyleSeed.class)
          .stream().map(ColorStyleSeed::toEntity)
          .toList();
    } catch (IOException e) {
      throw new RuntimeException(
          "Failed to read color style seed file: " + COLOR_STYLE_JSON_PATH,
          e);
    }
  }

  /**
   * color style 엔티티 목록을 기반으로 영속 상태의 ColorStyle Entity 리스트 반환
   * @return stylePropsName - ColorStyle 맵 반환
   * */
  private Map<String, ColorStyle> findPersistColorStyleMap(
      List<ColorStyle> transientSeedList) {
    List<String> identifierList = transientSeedList.stream()
        .map(ColorStyle::getStylePropsName)
        .toList();
    List<ColorStyle> persistList = colorStyleRepository.findByStylePropsNameIn(identifierList);
    return persistList.stream()
        .collect(Collectors.toMap(
            ColorStyle::getStylePropsName,
            Function.identity()));
  }

  /**
   * 내부 color style seed 문서(color_style.json)를 기반으로 color style을 upsert(update/insert)한다
   * */
  @Transactional
  public SeedResult upsertColorStyleSeed() {
    int created = 0, updated = 0;
    List<ColorStyle> transientSeedList = readColorStyleSeedList();
    Map<String, ColorStyle> persistMap = findPersistColorStyleMap(transientSeedList);
    for (ColorStyle transientSeed : transientSeedList) {
      String seedIdentifier = transientSeed.getStylePropsName();
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
