package com.komentum.catalog.repository;

import com.komentum.catalog.dto.ComponentSummary;
import com.komentum.user.domain.User;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface ComponentCatalogRepository {

  public List<ComponentSummary> findComponentSummaryByClient(Pageable pageable, User client);
}
