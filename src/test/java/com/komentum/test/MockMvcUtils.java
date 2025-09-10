package com.komentum.test;

import com.komentum.auth.AuthProperty;
import com.komentum.auth.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@Component
public class MockMvcUtils {

  @Autowired
  private JwtUtils jwtUtils;

  public MockHttpServletRequestBuilder generateAuthJsonRequest(
      MockHttpServletRequestBuilder requestBuilder, String userEmail) {
    String jwtToken = jwtUtils.generateAccessToken(userEmail);
    return requestBuilder.header(AuthProperty.ACCESS_TOKEN_HEADER,
            AuthProperty.ACCESS_TOKEN_PREFIX + " " + jwtToken)
        .contentType(MediaType.APPLICATION_JSON);
  }
}
