package com.komentum.test.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.util.MultiValueMap;

@Getter
@Builder
public class MockMvcRequestDto<T, R> {

  @NotNull
  private MockMvc mockMvc;

  @NotNull
  private String path;

  private MultiValueMap<String, String> params;

  private TestClientDto clientDto;

  @NotNull
  private HttpMethod httpMethod;

  private T body;

  @NotNull
  private TypeReference<R> responseType;

  @Builder.Default
  private int statusCode = 200;

  /**
   * 실제 요청에 들어가는 데이터만 관리하는 static class
   * */
  @Getter
  @Builder
  public static class ExecutionContext<E> {

    private final MockMvc mockMvc;
    private final MultiValueMap<String, String> params;
    private final E body;
    private final TestClientDto clientDto;

    public static <E> void addRequestInfoOnRequest(
        MockHttpServletRequestBuilder requestBuilder,
        ExecutionContext<E> context,
        ObjectMapper objectMapper) throws JsonProcessingException {
      if (context.getParams() != null) {
        requestBuilder.params(context.getParams());
      }
      if (context.getBody() != null) {
        requestBuilder.content(objectMapper.writeValueAsString(context.getBody()));
      }
    }
  }

  public ExecutionContext<T> toExecutionContext() {
    return ExecutionContext.<T>builder()
        .mockMvc(mockMvc)
        .params(params)
        .body(body)
        .clientDto(clientDto)
        .build();
  }
}
