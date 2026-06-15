package com.komentum.theme.component.service.seeder;

import com.fasterxml.jackson.databind.JsonNode;
import com.komentum.theme.utils.JsonUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public abstract class AbstractMapJsonSeeder<T> {

  private final JsonUtils jsonUtils;

  /**
   * 파싱할 json 파일의 리소스 경로를 나타낸다.
   * */
  protected abstract String getJsonPath();

  /**
   * json root에서 key:value 형태로 관리되는 데이터를 추출한다
   * */
  protected abstract JsonNode extractFromRoot(JsonNode root);

  /**
   * 키-값 형태의 json 데이터를 T Object로 변환
   * */
  protected abstract T convertToTarget(String key, JsonNode node);

  /**
   * 키-값 형태의 json 데이터를 읽고, T Object 목록 반환
   * */
  public List<T> readSeed() {
    try {
      JsonNode root = jsonUtils.readJsonNode(getJsonPath());
      JsonNode targetNode = extractFromRoot(root);
      if (targetNode == null || targetNode.isMissingNode() || !targetNode.isObject()) {
        throw new IllegalStateException("Invalid JSON Structure");
      }
      List<T> result = new ArrayList<>();
      targetNode.fields().forEachRemaining(entry -> {
        result.add(convertToTarget(entry.getKey(), entry.getValue()));
      });
      return result;
    } catch (IOException e) {
      throw new RuntimeException(
          "Failed to convert seed to target object: " + getJsonPath(),
          e);
    }
  }
}
