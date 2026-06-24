package com.komentum.designcomponent.repository;

import com.komentum.designcomponent.domain.ComponentType;
import com.komentum.designcomponent.enums.TypeCode;
import com.komentum.designcomponent.enums.Platform;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComponentTypeRepository extends JpaRepository<ComponentType, Integer> {

  List<ComponentType> findAllByTypeCodeIn(Collection<TypeCode> typeCodes);
}
