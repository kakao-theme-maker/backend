package com.komentum.global.properties;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpMethod;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

  private final String[] whiteList;
  private final SecurityRule[] adminOnly;
  private final String[] allowedOriginList;

  @Getter
  @Setter
  @NoArgsConstructor
  public static class SecurityRule {

    private String path;
    private List<HttpMethod> methods = new ArrayList<>();
  }

}

