package com.theme.repository;

import com.theme.domain.DesignComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DesignComponentRepository extends JpaRepository<DesignComponent, Integer> {

    // 즉시 로딩을 위한 커스텀 쿼리
    @Query("SELECT dc FROM DesignComponent dc JOIN FETCH dc.componentType WHERE dc.designComponentId = :id")
    Optional<DesignComponent> findByIdWithComponentType(@Param("id") Integer id);

    @Query("SELECT dc FROM DesignComponent dc JOIN FETCH dc.componentType")
    List<DesignComponent> findAllWithComponentType();

    @Query("SELECT dc FROM DesignComponent dc JOIN FETCH dc.componentType WHERE dc.userEmail = :email")
    List<DesignComponent> findByUserEmailWithComponentType(@Param("email") String email);

    @Query("SELECT dc FROM DesignComponent dc JOIN FETCH dc.componentType WHERE dc.isPublic = true")
    List<DesignComponent> findPublicWithComponentType();
}

