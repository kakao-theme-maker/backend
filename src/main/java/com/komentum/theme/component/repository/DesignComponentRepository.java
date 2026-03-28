package com.komentum.theme.component.repository;

import com.komentum.theme.component.domain.DesignComponent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DesignComponentRepository extends JpaRepository<DesignComponent, Integer> {

  List<DesignComponent> findByUser_UserEmail(String userUserEmail);

  List<DesignComponent> findByUser_UserEmailIn(List<String> userEmails);
}

