package com.komentum.global.properties;

import java.util.List;
import java.util.Map;
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

  private final Map<HttpMethod, List<SecurityRule>> permitAll;
  private final Map<HttpMethod, List<SecurityRule>> adminOnly;
  private final String[] allowedOriginList;

  public enum MatcherType {
    ANT,
    REGEX
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static class SecurityRule {

    private String pattern;
    private MatcherType matcher = MatcherType.ANT;
  }

}

