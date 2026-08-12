package com.komentum.designcomponent.service;

import com.komentum.designcomponent.domain.ComponentType;
import com.komentum.designcomponent.dto.ComponentTypeCreateRequest;
import com.komentum.designcomponent.dto.ComponentTypeUpdateRequest;
import com.komentum.designcomponent.enums.TypeCode;
import com.komentum.designcomponent.mapper.ComponentTypeMapper;
import com.komentum.designcomponent.repository.ComponentTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor // Lombok으로 생성자 주입 간소화
public class ComponentTypeService {

  private final ComponentTypeRepository componentTypeRepository;
  private final ComponentTypeMapper componentTypeMapper;

  /**
   * 파라미터를 기반으로 Component Type을 생성한다
   * @param request Component Type 생성에 필요한 데이터 DTO
   * @return 생성된 ComponentType 반환
   * */
  @Transactional
  public ComponentType createComponentType(ComponentTypeCreateRequest request) {
    ComponentType componentType = componentTypeMapper.toComponentType(request);
    return componentTypeRepository.save(componentType);
  }

  /**
   * ID를 기반으로 Component Type 조회
   * @param componentType component type의 ID
   * @return 조회된 componentType 반환
   * */
  @Transactional(readOnly = true)
  public ComponentType getComponentTypeById(Integer componentType) {
    return componentTypeRepository.findById(componentType)
        .orElseThrow(
            () -> new EntityNotFoundException("ComponentType not found with id: " + componentType));
  }

  /**
   * 모든 component type을 조회한다
   * @return 모든 componentType 반환
   * */
  @Transactional(readOnly = true)
  public List<ComponentType> getAllComponentTypes() {
    return componentTypeRepository.findAll();
  }

  /**
   * TypeCode - ComponentType map을 조회한다
   * */
  @Transactional(readOnly = true)
  public Map<TypeCode, ComponentType> findComponentTypeMap() {
    return componentTypeRepository.findAll()
        .stream()
        .collect(Collectors.toMap(
            ComponentType::getTypeCode,
            componentType -> componentType)
        );
  }

  /**
   * id=componentTypeId인 component type을 파라미터를 기반으로 갱신한다
   * @param componentTypeId 수정할 component type의 ID
   * @param request component type 수정을 위한 데이터 DTO
   * @return 수정된 componentType 반환
   * */
  @Transactional
  public ComponentType updateComponentType(Integer componentTypeId,
      ComponentTypeUpdateRequest request) {
    ComponentType componentType = getComponentTypeById(componentTypeId);
    componentType.update(request);
    return componentTypeRepository.save(componentType);
  }
}
