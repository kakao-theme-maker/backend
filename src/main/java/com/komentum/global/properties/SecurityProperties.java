package com.komentum.global.properties;

import com.komentum.global.dto.SecurityRule;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpMethod;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

  private final Map<HttpMethod, List<SecurityRule>> permitAll;
  private final Map<HttpMethod, List<SecurityRule>> guest;
  private final Map<HttpMethod, List<SecurityRule>> adminOnly;
  private final String[] allowedOriginList;

}

