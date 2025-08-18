package com.komentum.theme.component.repository;

import com.komentum.theme.component.domain.ColorStyle;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ColorStyleRepository extends JpaRepository<ColorStyle, Integer> {

    @Query("SELECT cs FROM ColorStyle cs WHERE cs.iosStyleName = :propertyName OR cs.androidStyleName = :propertyName")
    Optional<ColorStyle> findByIosStyleNameOrAndroidStyleName(@Param("propertyName") String iosStyleName, @Param("propertyName") String androidStyleName);
}
