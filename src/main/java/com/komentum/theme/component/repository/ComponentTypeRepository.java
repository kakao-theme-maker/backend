package com.komentum.theme.component.repository;

import com.komentum.theme.component.domain.ComponentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComponentTypeRepository extends JpaRepository<ComponentType, Integer> {

}
