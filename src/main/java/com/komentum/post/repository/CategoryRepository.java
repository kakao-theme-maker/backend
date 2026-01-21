package com.komentum.post.repository;

import com.komentum.post.domain.Category;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

  List<Category> findAllByOwner_UserEmail(String userUserEmail);
}
