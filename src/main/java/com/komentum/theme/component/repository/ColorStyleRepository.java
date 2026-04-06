package com.komentum.theme.component.repository;

import com.komentum.theme.component.domain.ColorStyle;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ColorStyleRepository extends JpaRepository<ColorStyle, Integer> {

  List<ColorStyle> findByStylePropsNameIn(Collection<String> stylePropsNameList);
}
