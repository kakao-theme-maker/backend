package com.komentum.post.repository;

import com.komentum.post.domain.Category;
import com.komentum.post.service.enums.CategoryType;
import com.komentum.user.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

  List<Category> findAllByOwner_UserEmail(String userUserEmail);

  Optional<Category> findByCategoryTypeAndOwner(CategoryType categoryType, User owner);

  @Query("select c from Category c "
      + "join fetch c.owner "
      + "where c.categoryType= :categoryType "
      + "and c.owner in :owners")
  List<Category> fetchJoinAllByCategoryTypeAndOwnerIn(CategoryType categoryType, List<User> owners);
}
