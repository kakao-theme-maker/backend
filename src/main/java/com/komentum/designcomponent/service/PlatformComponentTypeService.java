package com.komentum.designcomponent.service;

import com.komentum.designcomponent.domain.PlatformComponentType;
import com.komentum.designcomponent.enums.Platform;
import com.komentum.designcomponent.enums.TypeCode;
import com.komentum.designcomponent.repository.PlatformComponentTypeRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlatformComponentTypeService {

  private final PlatformComponentTypeRepository platformComponentTypeRepository;

  @Transactional(readOnly = true)
  public Map<TypeCode, List<PlatformComponentType>> findTypeCodeMapByPlatform(Platform platform) {
    return platformComponentTypeRepository.fetchJoinAllByPlatform(platform).stream()
        .collect(Collectors.groupingBy(pc -> pc.getComponentType().getTypeCode()));
  }
}
