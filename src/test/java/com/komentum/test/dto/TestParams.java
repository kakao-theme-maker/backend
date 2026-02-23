package com.komentum.test.dto;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class TestParams {

  public static final String PAGE_SIZE = "pageSize";
  public static final String PAGE_NUMBER = "pageNumber";
  public static final String USER_EMAIL = "user_email";

  public static MultiValueMap<String, String> withEmpty() {
    return new LinkedMultiValueMap<>();
  }

  public static MultiValueMap<String, String> withPaging(int pageNumber, int pageSize) {
    MultiValueMap<String, String> params = withEmpty();
    params.add(PAGE_SIZE, Integer.toString(pageSize));
    params.add(PAGE_NUMBER, Integer.toString(pageNumber));
    return params;
  }
}
