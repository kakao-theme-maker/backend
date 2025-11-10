package com.komentum.test;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.komentum.auth.AuthProperty;
import com.komentum.auth.JwtUtils;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.MultiValueMap;

@Component
public class MockMvcUtils {

  @Autowired
  private JwtUtils jwtUtils;

  @Autowired
  private ObjectMapper objectMapper;

  public MockHttpServletRequestBuilder generateAuthJsonRequest(
      MockHttpServletRequestBuilder requestBuilder, String userEmail) {
    String jwtToken = jwtUtils.generateAccessToken(userEmail);
    return requestBuilder.header(AuthProperty.ACCESS_TOKEN_HEADER,
            AuthProperty.ACCESS_TOKEN_PREFIX + " " + jwtToken)
        .contentType(MediaType.APPLICATION_JSON);
  }

  public String performAuthRequest(MockMvc mockMvc, MockHttpServletRequestBuilder requestBuilder,
      String userEmail) throws Exception {
    return mockMvc.perform(generateAuthJsonRequest(requestBuilder, userEmail))
        .andExpect(status().is2xxSuccessful())
        .andDo(print())
        .andReturn().getResponse().getContentAsString();
  }

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

  public <R> R requestGet(MockMvc mockMvc, String path,
      @Nullable MultiValueMap<String, String> params,
      String clientEmail,
      TypeReference<R> responseType)
      throws Exception {
    MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get(path);
    return executeRequest(mockMvc, requestBuilder, params, clientEmail, null, responseType);
  }

  public <T, R> R requestPost(MockMvc mockMvc, String path,
      @Nullable MultiValueMap<String, String> params,
      String clientEmail, T body, TypeReference<R> responseType) throws Exception {
    MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(path);
    return executeRequest(mockMvc, requestBuilder, params, clientEmail, body, responseType);
  }

  public <T, R> R requestPut(MockMvc mockMvc, String path,
      @Nullable MultiValueMap<String, String> params,
      String clientEmail, T body, TypeReference<R> responseType) throws Exception {
    MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.put(path);
    return executeRequest(mockMvc, requestBuilder, params, clientEmail, body, responseType);
  }

  public <T, R> R requestDelete(MockMvc mockMvc, String path,
      @Nullable MultiValueMap<String, String> params,
      String clientEmail, T body, TypeReference<R> responseType) throws Exception {
    MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.delete(path);
    return executeRequest(mockMvc, requestBuilder, params, clientEmail, body, responseType);
  }
}
