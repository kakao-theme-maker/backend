package com.komentum.config;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.komentum.designcomponent.enums.StyleCode;
import com.komentum.designcomponent.enums.TypeCode;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.BinarySchema;
import io.swagger.v3.oas.models.media.Encoding;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 * enum 값이 Swagger 화면에 드러나도록 API 문서를 보강한다. 허용 값은 enum 상수에서 직접 읽으므로, enum이 바뀌면 문서에도 자동으로 반영된다.
 */
@Configuration
@RequiredArgsConstructor
public class SwaggerEnumDocConfig {

  private static final String REQUEST_PART_NAME = "request";

  private final ObjectMapper objectMapper;

  /**
   * 요청에 사용하는 TypeCode, StyleCode 값 목록을 Swagger의 Schemas 영역에 enum 스키마로 등록한다. enum이 Map의 key이면 Swagger가
   * 허용 값을 표시하지 못하기 때문이다. 값은 enum 상수에서 직접 읽으므로 enum이 바뀌면 문서에도 자동으로 반영된다.
   */
  @Bean
  public OpenApiCustomizer codeEnumSchemaCustomizer() {
    return openApi -> {
      if (openApi.getComponents() == null) {
        openApi.setComponents(new Components());
      }
      List.of(TypeCode.class, StyleCode.class).forEach(enumClass -> {
        StringSchema schema = new StringSchema();
        schema.setEnum(enumJsonValues(enumClass));
        schema.setDescription("요청에 사용할 수 있는 " + enumClass.getSimpleName() + " 값 목록");
        openApi.getComponents().addSchemas(enumClass.getSimpleName(), schema);
      });
    };
  }

  /**
   * TypeCode 이름의 이미지 part를 받는 API(이름 없는 MultiValueMap&lt;String, MultipartFile&gt; 파라미터)의 문서를 보강한다. springdoc이
   * 이 파라미터를 필수 query 파라미터로 잘못 노출하므로 제거하고, multipart 스키마에 TypeCode별 파일 선택 항목을 추가한다.
   */
  @Bean
  public OperationCustomizer typeCodeImagePartOperationCustomizer() {
    return (operation, handlerMethod) -> {
      MethodParameter imageMapParameter = Arrays.stream(handlerMethod.getMethodParameters())
          .filter(this::isImageMapParameter)
          .findFirst().orElse(null);
      if (imageMapParameter == null || operation.getRequestBody() == null
          || operation.getRequestBody().getContent() == null) {
        return operation;
      }
      MediaType multipart = operation.getRequestBody().getContent()
          .get(org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE);
      if (multipart == null || multipart.getSchema() == null) {
        return operation;
      }
      // springdoc이 query 파라미터로 잘못 노출한 이미지 맵 파라미터 제거
      imageMapParameter.initParameterNameDiscovery(new DefaultParameterNameDiscoverer());
      String parameterName = imageMapParameter.getParameterName();
      if (operation.getParameters() != null) {
        operation.getParameters()
            .removeIf(p -> "query".equals(p.getIn()) && p.getName().equals(parameterName));
      }
      // TypeCode별 이미지 파일 선택 항목 추가
      Schema<?> schema = multipart.getSchema();
      enumJsonValues(TypeCode.class).forEach(typeCodeName ->
          schema.addProperty(typeCodeName, new BinarySchema()
              .description("`" + typeCodeName + "` 이미지 (PNG, 선택). 파일을 보내면 교체, 보내지 않으면 유지")));
      // JSON part는 application/json으로 전송
      multipart.addEncoding(REQUEST_PART_NAME, new Encoding().contentType("application/json"));
      return operation;
    };
  }

  /**
   * 이름 없는 @RequestParam MultiValueMap&lt;String, MultipartFile&gt; 파라미터인지 확인한다.
   */
  private boolean isImageMapParameter(MethodParameter parameter) {
    RequestParam requestParam = parameter.getParameterAnnotation(RequestParam.class);
    if (requestParam == null || !MultiValueMap.class.isAssignableFrom(
        parameter.getParameterType())) {
      return false;
    }
    JavaType javaType = Json.mapper().constructType(parameter.getGenericParameterType());
    return javaType.containedTypeCount() == 2
        && MultipartFile.class.isAssignableFrom(javaType.containedType(1).getRawClass());
  }

  /**
   * enum 상수를 API 요청/응답에서 사용하는 값(@JsonValue 등 Jackson 직렬화 결과)으로 변환한다.
   */
  private List<String> enumJsonValues(Class<?> enumClass) {
    return Arrays.stream(enumClass.getEnumConstants())
        .map(constant -> objectMapper.convertValue(constant, String.class))
        .toList();
  }
}
