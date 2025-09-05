package com.komentum.theme.component.service;

import com.komentum.theme.component.domain.ComponentType;
import com.komentum.theme.component.repository.ComponentTypeRepository;
import com.komentum.theme.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor // Lombok으로 생성자 주입 간소화
public class ComponentTypeService {

  private final ComponentTypeRepository componentTypeRepository;

  @Transactional
  public ComponentType createComponentType(ComponentType componentType) {
    return componentTypeRepository.save(componentType);
  }

  @Transactional(readOnly = true)
  public ComponentType getComponentTypeById(Integer id) {
    return componentTypeRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("ComponentType not found with id: " + id));
  }

  @Transactional(readOnly = true)
  public List<ComponentType> getAllComponentTypes() {
    return componentTypeRepository.findAll();
  }

  public ComponentType updateComponentType(Integer id, ComponentType componentTypeDetails) {
    ComponentType componentType = getComponentTypeById(id);

    Optional.ofNullable(componentTypeDetails.getExplain()).ifPresent(componentType::setExplain);
    Optional.ofNullable(componentTypeDetails.getIosComponentPath())
        .ifPresent(componentType::setIosComponentPath);
    Optional.ofNullable(componentTypeDetails.getIosComponentName())
        .ifPresent(componentType::setIosComponentName);
    Optional.ofNullable(componentTypeDetails.getAndroidComponentPath())
        .ifPresent(componentType::setAndroidComponentPath);
    Optional.ofNullable(componentTypeDetails.getAndroidComponentName())
        .ifPresent(componentType::setAndroidComponentName);
    Optional.ofNullable(componentTypeDetails.getSizeX()).ifPresent(componentType::setSizeX);
    Optional.ofNullable(componentTypeDetails.getSizeY()).ifPresent(componentType::setSizeY);

    return componentTypeRepository.save(componentType);
  }

  public void deleteComponentType(Integer id) {
    if (!componentTypeRepository.existsById(id)) {
      throw new ResourceNotFoundException("ComponentType not found with id: " + id);
    }
    componentTypeRepository.deleteById(id);
  }
}
