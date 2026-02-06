package com.komentum.test.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.util.MultiValueMap;

@Getter
@Builder
@AllArgsConstructor
public class MockMvcMultipartRequestDto<R> {

  @NotNull
  private MockMvc mockMvc;

  @NotNull
  private String path;

  @NotNull
  private HttpMethod httpMethod;

  private MultiValueMap<String, String> params;

  private List<MockMultipartFile> formDataList;

  private TestClientDto clientDto;

  @NotNull
  private TypeReference<R> responseType;

  @Builder.Default
  private int statusCode = 200;

  /**
   * 실제 요청에 들어가는 데이터만 관리하는 static class
   * */
  @Getter
  @Builder
  public static class MultipartExecutionContext {

    private final MockMvc mockMvc;
    private final MultiValueMap<String, String> params;
    private final List<MockMultipartFile> formDataList;
    private final TestClientDto clientDto;

    public static void addRequestInfoOnRequest(
        MockMultipartHttpServletRequestBuilder requestBuilder, MultipartExecutionContext context) {
      if (context.getFormDataList() != null) {
        context.getFormDataList().forEach(requestBuilder::file);
      }
      if (context.getParams() != null) {
        requestBuilder.params(context.getParams());
      }
    }
  }

  public MultipartExecutionContext toExecutionContext() {
    return MultipartExecutionContext.builder()
        .mockMvc(mockMvc)
        .params(params)
        .formDataList(formDataList)
        .clientDto(clientDto)
        .build();
  }

  public String getHttpMethodName() {
    return this.httpMethod.name();
  }

}
