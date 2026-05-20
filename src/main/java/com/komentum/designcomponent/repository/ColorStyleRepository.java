package com.komentum.designcomponent.repository;

import com.komentum.designcomponent.domain.ColorStyle;
import com.komentum.designcomponent.enums.StyleCode;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ColorStyleRepository extends JpaRepository<ColorStyle, Integer> {

  List<ColorStyle> findAllByStyleCodeIn(Collection<StyleCode> styleCodes);
}
