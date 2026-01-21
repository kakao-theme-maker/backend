package com.komentum.test;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.komentum.auth.AuthProperty;
import com.komentum.auth.JwtUtils;
import jakarta.annotation.Nullable;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.MultiValueMap;

@Component
public class MockMvcUtils {

  @Autowired
  private JwtUtils jwtUtils;

  @Autowired
  private ObjectMapper objectMapper;

  private static final MediaType DEFAULT_CONTENT_TYPE = MediaType.APPLICATION_JSON;

  /**
   * 인증 정보가 포함된 RequestBuilder 생성
   * */
  public MockHttpServletRequestBuilder generateAuthJsonRequest(
      MockHttpServletRequestBuilder requestBuilder, String userEmail) {
    String jwtToken = jwtUtils.generateAccessToken(userEmail);
    return requestBuilder.header(AuthProperty.ACCESS_TOKEN_HEADER,
        AuthProperty.ACCESS_TOKEN_PREFIX + " " + jwtToken);
  }

  /**
   * 인증이 필요한 요청 수행 후 응답 코드 검증 후 결과 반환
   * */
  public String performAuthRequest(MockMvc mockMvc, MockHttpServletRequestBuilder requestBuilder,
      String userEmail) throws Exception {
    return mockMvc.perform(generateAuthJsonRequest(requestBuilder, userEmail))
        .andExpect(status().is2xxSuccessful())
        .andDo(print())
        .andReturn().getResponse().getContentAsString();
  }

  /**
   * 일반 HTTP 요청 수행 및 결과 반환
   * */
  private <T, R> R executeRequest(MockMvc mockMvc, MockHttpServletRequestBuilder requestBuilder,
      @Nullable MultiValueMap<String, String> params, String clientEmail,
      @Nullable T body, TypeReference<R> responseType) throws Exception {
    if (params != null) {
      requestBuilder.params(params);
    }
    if (body != null) {
      requestBuilder.content(objectMapper.writeValueAsString(body));
    }
    String response = performAuthRequest(mockMvc, requestBuilder, clientEmail);
    if (responseType.getType() == Void.class) {
      return null;
    }
    return objectMapper.readValue(response, responseType);
  }

  /**
   * Form 요청 수행 및 결과 반환
   * */
  private <T, R> R executeRequest(MockMvc mockMvc,
      MockMultipartHttpServletRequestBuilder requestBuilder,
      @Nullable MultiValueMap<String, String> params,
      String clientEmail,
      @Nullable List<MockMultipartFile> formDataList,
      TypeReference<R> responseType) throws Exception {
    if (params != null) {
      requestBuilder.params(params);
    }
    if (formDataList != null) {
      formDataList.forEach(requestBuilder::file);
    }
    String response = performAuthRequest(mockMvc, requestBuilder, clientEmail);
    if (responseType.getType() == Void.class) {
      return null;
    }
    return objectMapper.readValue(response, responseType);
  }

  /**
   * 인증이 필요한 GET 요청 수행 후 결과 반환
   * */
  public <R> R requestGet(MockMvc mockMvc, String path,
      @Nullable MultiValueMap<String, String> params,
      String clientEmail,
      TypeReference<R> responseType)
      throws Exception {
    MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get(path);
    requestBuilder.contentType(DEFAULT_CONTENT_TYPE);
    return executeRequest(mockMvc, requestBuilder, params, clientEmail, null, responseType);
  }

  /**
   * 인증이 필요한 Post 요청 수행 후 결과 반환
   * */
  public <T, R> R requestPost(MockMvc mockMvc, String path,
      @Nullable MultiValueMap<String, String> params,
      String clientEmail, T body, TypeReference<R> responseType) throws Exception {
    MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(path);
    requestBuilder.contentType(DEFAULT_CONTENT_TYPE);
    return executeRequest(mockMvc, requestBuilder, params, clientEmail, body, responseType);
  }

  /**
   * 인증이 필요한 form 요청 수행 후 결과 반환
   * */
  public <R> R performMultipartRequest(MockMvc mockMvc, String path, HttpMethod method,
      @Nullable MultiValueMap<String, String> params,
      String clientEmail, List<MockMultipartFile> formDataList, TypeReference<R> responseType)
      throws Exception {
    MockMultipartHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.multipart(path);
    requestBuilder.with(request -> {
      request.setMethod(method.name());
      return request;
    });
    return executeRequest(mockMvc, requestBuilder, params, clientEmail, formDataList, responseType);
  }

  /**
   * 인증이 필요한 PUT 요청 수행 후 결과 반환
   * */
  public <T, R> R requestPut(MockMvc mockMvc, String path,
      @Nullable MultiValueMap<String, String> params,
      String clientEmail, T body, TypeReference<R> responseType) throws Exception {
    MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.put(path);
    requestBuilder.contentType(DEFAULT_CONTENT_TYPE);
    return executeRequest(mockMvc, requestBuilder, params, clientEmail, body, responseType);
  }

  /**
   * 인증이 필요한 PATCH 요청 수행 후 결과 반환
   * */
  public <T, R> R requestPatch(MockMvc mockMvc, String path,
      @Nullable MultiValueMap<String, String> params,
      String clientEmail, T body, TypeReference<R> responseType) throws Exception {
    MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.patch(path);
    requestBuilder.contentType(DEFAULT_CONTENT_TYPE);
    return executeRequest(mockMvc, requestBuilder, params, clientEmail, body, responseType);
  }

  /**
   * 인증이 필요한 DELETE 요청 수행 후 결과 반환
   * */
  public <T, R> R requestDelete(MockMvc mockMvc, String path,
      @Nullable MultiValueMap<String, String> params,
      String clientEmail, T body, TypeReference<R> responseType) throws Exception {
    MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.delete(path);
    requestBuilder.contentType(DEFAULT_CONTENT_TYPE);
    return executeRequest(mockMvc, requestBuilder, params, clientEmail, body, responseType);
  }

  /**
   * 테스트용 파일에 대한 form 데이터 생성
   * */
  public MockMultipartFile fileToTestFormData(String fileName, String originName,
      MediaType contentType, byte[] content) {
    return new MockMultipartFile(
        fileName,
        originName,
        contentType.toString(),
        content
    );
  }

  /**
   * 테스트용 json에 대한 form 데이터 생성
   * */
  public <T> MockMultipartFile jsonToTestFormData(String fileName,
      T object)
      throws Exception {
    return new MockMultipartFile(
        fileName,
        String.join(fileName, "json"),
        MediaType.APPLICATION_JSON.toString(),
        objectMapper.writeValueAsBytes(object));
  }
}
