package com.komentum.utils;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.komentum.auth.AuthProperty;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@Component
public class MockMvcUtils {

  @Autowired
  private ObjectMapper objectMapper;

  public String performAuthRequest(MockMvc mockMvc, MockHttpServletRequestBuilder requestBuilder,
      String token) throws Exception {
    return mockMvc.perform(requestBuilder
            .contentType(MediaType.APPLICATION_JSON)
            .header(AuthProperty.ACCESS_TOKEN_HEADER,
                AuthProperty.ACCESS_TOKEN_PREFIX + " " + token)).andExpect(status().is2xxSuccessful())
        .andReturn().getResponse().getContentAsString();
  }

  public <T> T performAuthRequest(MockMvc mockMvc,
      MockHttpServletRequestBuilder requestBuilder,
      String token, Class<T> clazz) throws Exception {
    String json = performAuthRequest(mockMvc, requestBuilder, token);
    return objectMapper.readValue(json, clazz);
  }

  public <T> List<T> performAuthRequestForList(MockMvc mockMvc,
      MockHttpServletRequestBuilder requestBuilder, String token) throws Exception {
    String json = performAuthRequest(mockMvc, requestBuilder, token);
    return objectMapper.readValue(json, new TypeReference<>() {
    });
  }
}
