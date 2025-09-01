package com.komentum.theme.theme.service;

import com.komentum.theme.exception.ResourceNotFoundException;
import com.komentum.theme.theme.domain.ThemeComponent;
import com.komentum.theme.theme.dto.ThemeComponentDto;
import com.komentum.theme.theme.mapper.ThemeComponentMapper;
import com.komentum.theme.theme.repository.ThemeComponentRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ThemeRetrieveService {

  private final ThemeComponentRepository themeComponentRepository;
  private final ThemeComponentMapper themeComponentMapper;

  @Transactional(readOnly = true)
  public List<ThemeComponentDto> getAllThemes() {
    return themeComponentRepository.fetchJoinAll().stream()
        .map(themeComponentMapper::convertToDto)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<ThemeComponentDto> getThemesByUserEmail(String userEmail) {
    return themeComponentRepository.fetchJoinByUserEmail(userEmail).stream()
        .map(themeComponentMapper::convertToDto)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<ThemeComponentDto> getPublicThemes() {
    return themeComponentRepository.fetchJoinByIsPublicTrue().stream()
        .map(themeComponentMapper::convertToDto)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public ThemeComponentDto getThemeById(Integer id) {
    ThemeComponent themeComponent = themeComponentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Theme not found with id: " + id));
    return themeComponentMapper.convertToDto(themeComponent);
  }

  @Transactional(readOnly = true)
  public List<ThemeComponentDto> getCompletedThemes() {
    List<ThemeComponent> completedThemes = themeComponentRepository.fetchJoinByIsDoneTrue();
    return completedThemes.stream()
        .map(themeComponentMapper::convertToDto)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<ThemeComponentDto> getCompletedThemesByUser(String userEmail) {
    List<ThemeComponent> completedThemes = themeComponentRepository.fetchJoinByUserEmailAndIsDoneTrue(
        userEmail);
    return completedThemes.stream()
        .map(themeComponentMapper::convertToDto)
        .collect(Collectors.toList());
  }
}
