package com.komentum.designcomponent.service;

import com.komentum.designcomponent.domain.PlatformColorStyle;
import com.komentum.designcomponent.enums.Platform;
import com.komentum.designcomponent.enums.StyleCode;
import com.komentum.designcomponent.repository.PlatformColorStyleRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlatformColorStyleService {

  private final PlatformColorStyleRepository platformColorStyleRepository;

  @Transactional(readOnly = true)
  public Map<StyleCode, List<PlatformColorStyle>> findStyleCodeMapByPlatform(
      Platform platform) {
    return platformColorStyleRepository.fetchJoinAllByPlatform(platform).stream()
        .collect(Collectors.groupingBy(pcs -> pcs.getColorStyle().getStyleCode()));
  }
}
