package com.komentum.test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.komentum.auth.AuthProperty;
import com.komentum.auth.JwtUtils;
import com.komentum.test.dto.MockMvcMultipartRequestDto;
import com.komentum.test.dto.MockMvcMultipartRequestDto.MultipartExecutionContext;
import com.komentum.test.dto.MockMvcRequestDto;
import com.komentum.test.dto.MockMvcRequestDto.ExecutionContext;
import com.komentum.test.dto.TestClientDto;
import java.io.UnsupportedEncodingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@Component
public class MockMvcUtils {

  @Autowired
  private JwtUtils jwtUtils;

  @Autowired
  private ObjectMapper objectMapper;

  private static final MediaType DEFAULT_CONTENT_TYPE = MediaType.APPLICATION_JSON;

  /**
   * 책임 : requestBuilder에 토큰 정보 추가 인증 정보를 기반으로 토큰을 생성하고 주입한 RequestBuilder 반환
   */
  public MockHttpServletRequestBuilder addAuthentication(
      MockHttpServletRequestBuilder requestBuilder, TestClientDto clientDto) {
    return addAuthentication(requestBuilder, clientDto.getPublicUserId());
  }

  /**
   * 책임 : requestBuilder에 토큰 정보 추가 이메일을 기반으로 토큰을 생성하여 주입한 RequestBuilder 반환
   */
  @Deprecated
  public MockHttpServletRequestBuilder addAuthentication(
      MockHttpServletRequestBuilder requestBuilder, String userIdentifier) {
    String jwtToken = jwtUtils.generateAccessToken(userIdentifier);
    return requestBuilder.header(AuthProperty.ACCESS_TOKEN_HEADER,
        AuthProperty.ACCESS_TOKEN_PREFIX + " " + jwtToken);
  }

  /**
   * 책임 : 요청에 필요한 데이터를 넣고, mockMvc.perform으로 요청 수행 일반 HTTP 요청용 request builder에 요청 수행 시 필요한 정보를
   * 추가하고, HTTP 요청 결과 반환
   */
  public <E> ResultActions performAuthRequest(
      MockHttpServletRequestBuilder requestBuilder,
      ExecutionContext<E> context) throws Exception {
    // params, body 설정
    ExecutionContext.addRequestInfoOnRequest(requestBuilder, context, objectMapper);
    // content-type 설정 ( 추후 별도 커스텀 헤더 필요 시, addRequestInfoOnRequest로 합칠 예정 )
    requestBuilder.contentType(DEFAULT_CONTENT_TYPE);
    return context.getMockMvc()
        .perform(addAuthentication(requestBuilder, context.getClientDto()));
  }

  /**
   * 책임 : 요청에 필요한 데이터를 넣고, mockMvc.perform으로 요청 수행 Multipart HTTP 요청용 request builder에 요청 수행 시 필요한
   * 정보를 추가하고, HTTP 요청 결과 반환
   */
  public ResultActions performMultipartAuthRequest(
      MockMultipartHttpServletRequestBuilder requestBuilder,
      MultipartExecutionContext context) throws Exception {
    MultipartExecutionContext.addRequestInfoOnRequest(requestBuilder, context);
    return context.getMockMvc()
        .perform(addAuthentication(requestBuilder, context.getClientDto()));
  }

  /**
   * 책임 : ResultActions의 응답값을 responseType으로 변환 ResultActions의 응답값을 responseType으로 변환 후 반환
   */
  public <R> R parseResponse(ResultActions result, TypeReference<R> responseType)
      throws UnsupportedEncodingException, JsonProcessingException {
    String response = result.andReturn().getResponse().getContentAsString();
    if (responseType.getType() == Void.class) {
      return null;
    }
    return objectMapper.readValue(response, responseType);
  }

  /**
   * 일반 HTTP 요청에 대해 DTO를 기반으로 요청을 수행하고 상태 코드 검증 후, 결과를 R 타입으로 변환
   */
  public <T, R> R doAuthRequest(MockMvcRequestDto<T, R> requestDto) throws Exception {
    MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.request(
        requestDto.getHttpMethod(), requestDto.getPath());
    // 요청에 필요한 데이터 추가 및 요청 수행 + 상태값 검증
    ResultActions resultActions = performAuthRequest(requestBuilder,
        requestDto.toExecutionContext())
        .andExpect(status().is(requestDto.getStatusCode()));
    // 결과를 ResponseType으로 변환하여 반환
    return parseResponse(resultActions, requestDto.getResponseType());
  }

  /**
   * Multipart 요청에 대해 DTO를 기반으로 요청을 수행하고, 상태 코드 검증 후, 결과를 R 타입으로 변환
   */
  public <R> R doAuthMultipartRequest(MockMvcMultipartRequestDto<R> requestDto) throws Exception {
    MockMultipartHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.multipart(
        requestDto.getPath());
    // HTTP 메서드 설정
    requestBuilder.with(request -> {
      request.setMethod(requestDto.getHttpMethodName());
      return request;
    });
    // 요청에 필요한 데이터 추가 및 요청 수행 + 상태값 검증
    ResultActions resultActions = performMultipartAuthRequest(requestBuilder,
        requestDto.toExecutionContext())
        .andExpect(status().is(requestDto.getStatusCode()));
    // 결과를 ResponseType으로 변환하여 반환
    return parseResponse(resultActions, requestDto.getResponseType());
  }

  /**
   * 테스트용 파일에 대한 form 데이터 생성
   */
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
   */
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
